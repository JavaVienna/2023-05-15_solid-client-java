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

import java.net.URI;
import java.util.Objects;

/**
 * A class for representing a Token request.
 */
public final class TokenRequest {

    private String grantType;
    private String code;
    private String codeVerifier;
    private String clientId;
    private String clientSecret;
    private String authMethod;
    private URI redirectUri;

    /**
     * Get the grant type value.
     *
     * @return the grant type
     */
    public String getGrantType() {
        return grantType;
    }

    /**
     * Get the authentication method.
     *
     * @return the authentication method
     */
    public String getAuthMethod() {
        return authMethod;
    }

    /**
     * Get the authorization code value.
     *
     * @return the authorization code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the code_verifier value.
     *
     * @return the code verifier
     */
    public String getCodeVerifier() {
        return codeVerifier;
    }

    /**
     * Get the client_id value.
     *
     * @return the client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the client_secret value.
     *
     * @return the client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Get the redirect_uri value.
     *
     * @return the redirect URI
     */
    public URI getRedirectUri() {
        return redirectUri;
    }

    /**
     * Create a new token request builder.
     *
     * @return the new builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private TokenRequest() {
        // Prevent external instantiation
    }

    /**
     * A builder class for {@link TokenRequest} objects.
     */
    public static class Builder {

        private String builderClientSecret;
        private String builderAuthMethod;
        private String builderCode;
        private String builderCodeVerifier;

        /**
         * Set the client secret value.
         *
         * @param clientSecret the client secret
         * @return this builder
         */
        public Builder clientSecret(final String clientSecret) {
            builderClientSecret = clientSecret;
            return this;
        }

        /**
         * Set the PKCE code verifier for the token endpoint.
         *
         * @param codeVerifier the code verifier
         * @return this builder
         */
        public Builder codeVerifier(final String codeVerifier) {
            builderCodeVerifier = codeVerifier;
            return this;
        }

        /**
         * Set the authentication method for the token endpoint.
         *
         * @param authMethod the authentication method
         * @return this builder
         */
        public Builder authMethod(final String authMethod) {
            builderAuthMethod = authMethod;
            return this;
        }

        /**
         * Set the authorization code value.
         *
         * @param code the authorization code
         * @return this builder
         */
        public Builder code(final String code) {
            builderCode = code;
            return this;
        }

        /**
         * Build a token request.
         *
         * @param grantType the grant type
         * @param clientId the client id
         * @param redirectUri the redirect URI
         * @return the token request
         */
        public TokenRequest build(final String grantType, final String clientId, final URI redirectUri) {

            final var grant = Objects.requireNonNull(grantType);

            if ("authorization_code".equals(grantType)) {
                if (builderCode == null) {
                    throw new IllegalArgumentException(
                            "Missing code parameter for authorization_code grant type");
                }
            } else if ("client_credentials".equals(grantType)) {
                if (builderClientSecret == null) {
                    throw new IllegalArgumentException(
                            "Missing client_secret parameter for client_credentials grant type");
                }
            }

            final var req = new TokenRequest();
            req.redirectUri = Objects.requireNonNull(redirectUri);
            req.clientId = Objects.requireNonNull(clientId);
            req.grantType = grant;
            req.code = builderCode;
            req.clientSecret = builderClientSecret;
            req.codeVerifier = builderCodeVerifier;
            req.authMethod = builderAuthMethod;

            return req;
        }
    }
}
