/*
 * Copyright 2022 Inrupt Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.inrupt.client.openid;

import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.openid.TokenRequest.Builder;
import com.inrupt.client.spi.HttpService;
import com.inrupt.client.spi.ServiceProvider;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OpenIdProviderTest {

    private static final HttpService client = ServiceProvider.getHttpService();
    private static OpenIdProvider openIdProvider;
    private static final OpenIdMockHttpService mockHttpService = new OpenIdMockHttpService();
    private static final Map<String, String> config = new HashMap<>();


    @BeforeAll
    static void setup() throws NoSuchAlgorithmException {
        config.put("openid_uri", mockHttpService.start());
        openIdProvider = new OpenIdProvider(URI.create(config.get("openid_uri")), client);
    }

    @AfterAll
    static void teardown() {
        mockHttpService.stop();
    }

    @Test
    void metadataTest() {
        assertEquals("http://example.test", openIdProvider.metadata().issuer.toString());
        assertEquals("http://example.test/oauth/jwks", openIdProvider.metadata().jwksUri.toString());
    }

    @Test
    void metadataAsyncTest() {
        assertEquals("http://example.test",
                openIdProvider.metadataAsync().toCompletableFuture().join().issuer.toString());
        assertEquals("http://example.test/oauth/jwks",
                openIdProvider.metadataAsync().toCompletableFuture().join().jwksUri.toString());
    }

    @Test
    void authorizeTest() {
        final AuthorizationRequest authReq = AuthorizationRequest.newBuilder()
            .codeChallenge("myCodeChallenge")
            .codeChallengeMethod("method")
            .build(
                "myClientId",
                URI.create("myRedirectUri")
        );
        assertEquals(
            "http://example.test/auth?client_id=myClientId&redirect_uri=myRedirectUri&" +
            "response_type=code&code_challenge=myCodeChallenge&code_challenge_method=method",
            openIdProvider.authorizeAsync(authReq).toCompletableFuture().join().toString()
        );
    }

    @Test
    void authorizeAsyncTest() {
        final AuthorizationRequest authReq = AuthorizationRequest.newBuilder()
            .codeChallenge("myCodeChallenge")
            .codeChallengeMethod("method")
            .build(
                "myClientId",
                URI.create("myRedirectUri")
            );
        assertEquals(
            "http://example.test/auth?client_id=myClientId&redirect_uri=myRedirectUri&" +
            "response_type=code&code_challenge=myCodeChallenge&code_challenge_method=method",
            openIdProvider.authorizeAsync(authReq).toCompletableFuture().join().toString()
        );
    }

    @Test
    void tokenRequestIllegalArgumentsTest() {
        final Builder builder = TokenRequest.newBuilder();
        final URI uri = URI.create("myRedirectUri");
        assertThrows(
            NullPointerException.class,
            () -> builder.build(null,"myClientId",uri));
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.build("authorization_code", "myClientId", uri));
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.build("client_credentials", "myClientId", uri));
        assertThrows(
            NullPointerException.class,
            () -> builder.build("myGrantType", null, uri));
        assertThrows(
            NullPointerException.class,
            () -> builder.build("myGrantType", "myClientId", null));
    }

    @Test
    void tokenNoClientSecretTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .build(
                "authorization_code",
                "myClientId",
                URI.create("https://example.test/redirectUri")
            );
        final TokenResponse token = openIdProvider.token(tokenReq);
        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("123456", token.idToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void tokenWithClientSecretBasicTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .clientSecret("myClientSecret")
            .authMethod("client_secret_basic")
            .build(
                "authorization_code",
                "myClientId",
                URI.create("https://example.test/redirectUri")
            );
        final TokenResponse token = openIdProvider.token(tokenReq);
        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("123456", token.idToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void tokenWithClientSecretePostTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder()
            .code("someCode")
            .codeVerifier("myCodeverifier")
            .clientSecret("myClientSecret")
            .authMethod("client_secret_post")
            .build(
                "authorization_code",
                "myClientId",
                URI.create("https://example.test/redirectUri")
            );
        final TokenResponse token = openIdProvider.token(tokenReq);
        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("123456", token.idToken);
        assertEquals("Bearer", token.tokenType);
    }

    @Test
    void tokenAsyncTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder().code("someCode")
                .codeVerifier("myCodeverifier").build("authorization_code", "myClientId",
                        URI.create("https://example.test/redirectUri"));
        final TokenResponse token = openIdProvider.tokenAsync(tokenReq).toCompletableFuture().join();
        assertEquals("token-from-id-token", token.accessToken);
        assertEquals("123456", token.idToken);
        assertEquals("Bearer", token.tokenType);

    }

    @Test
    void tokenAsyncStatusCodesTest() {
        final TokenRequest tokenReq = TokenRequest.newBuilder().code("none")
                .codeVerifier("none").build("authorization_code", "none",
                        URI.create("none"));

        assertAll("Not found",
            () -> {
                final CompletionStage<TokenResponse> completionStage = openIdProvider.tokenAsync(tokenReq);
                final CompletableFuture<TokenResponse> completableFuture = completionStage.toCompletableFuture();
                final CompletionException exception = assertThrows(CompletionException.class,
                    () -> completableFuture.join()
                );
                assertTrue(exception.getCause() instanceof OpenIdException);
                final OpenIdException cause = (OpenIdException) exception.getCause();

                assertEquals(
                    "Unexpected error while interacting with the OpenID Provider's token endpoint.",
                    cause.getMessage());
                assertEquals(OptionalInt.of(404), cause.getStatus());
            });
    }

    @Test
    void endSessionTest() {
        final EndSessionRequest endReq = EndSessionRequest.Builder.newBuilder()
            .postLogoutRedirectUri(URI.create("https://example.test/redirectUri"))
            .clientId("myClientId")
            .state("solid")
            .build();
        final URI uri = openIdProvider.endSession(endReq);
        assertEquals(
            "http://example.test/endSession?" +
            "client_id=myClientId&post_logout_redirect_uri=https://example.test/redirectUri&id_token_hint=&state=solid",
            uri.toString()
        );
    }

    @Test
    void endSessionAsyncTest() {
        final EndSessionRequest endReq = EndSessionRequest.Builder.newBuilder()
            .postLogoutRedirectUri(URI.create("https://example.test/redirectUri"))
            .clientId("myClientId")
            .state("solid")
            .build();
        final URI uri = openIdProvider.endSessionAsync(endReq).toCompletableFuture().join();
        assertEquals(
            "http://example.test/endSession?" +
            "client_id=myClientId&post_logout_redirect_uri=https://example.test/redirectUri&id_token_hint=&state=solid",
            uri.toString()
        );
    }
}