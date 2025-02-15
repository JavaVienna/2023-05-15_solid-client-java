/*
 * Copyright Inrupt Inc.
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
package com.inrupt.client.uma;

import com.inrupt.client.ClientCache;
import com.inrupt.client.Request;
import com.inrupt.client.auth.Authenticator;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.Session;
import com.inrupt.client.spi.ServiceProvider;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A session implementation for use with UMA Authorization Servers.
 *
 * @deprecated As of Beta3, this class is deprecated
 */
@Deprecated
public final class UmaSession implements Session {

    private final String id;
    private final Set<String> schemes;
    private final List<Session> internalSessions = new ArrayList<>();
    private final ClientCache<URI, Credential> tokenCache;

    private UmaSession(final Session... sessions) {
        this.id = UUID.randomUUID().toString();
        final Set<String> schemeTypes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (final Session session : sessions) {
            this.internalSessions.add(session);
            schemeTypes.addAll(session.supportedSchemes());
        }
        schemeTypes.add("UMA");
        this.schemes = Collections.unmodifiableSet(schemeTypes);
        this.tokenCache = ServiceProvider.getCacheBuilder().build(1000, Duration.ofMinutes(5));
    }

    /**
     * Create a session by wrapping other sessions.
     *
     * @param sessions the wrapped sessions
     * @return the session
     */
    public static Session of(final Session... sessions) {
        return new UmaSession(sessions);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void reset() {
        for (final Session session : internalSessions) {
            session.reset();
        }
        tokenCache.invalidateAll();
    }

    @Override
    public Optional<URI> getPrincipal() {
        for (final Session session : internalSessions) {
            final Optional<URI> principal = session.getPrincipal();
            if (principal.isPresent()) {
                return principal;
            }
        }
        return Optional.empty();
    }

    @Override
    public Set<String> supportedSchemes() {
        return schemes;
    }

    @Override
    public Optional<Credential> getCredential(final URI name, final URI uri) {
        for (final Session session : internalSessions) {
            final Optional<Credential> credential = session.getCredential(name, uri);
            if (credential.isPresent()) {
                return credential;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> generateProof(final String jkt, final Request request) {
        for (final Session session : internalSessions) {
            final Optional<String> proof = session.generateProof(jkt, request);
            if (proof.isPresent()) {
                return proof;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> selectThumbprint(final Collection<String> algorithms) {
        for (final Session session : internalSessions) {
            final Optional<String> jkt = session.selectThumbprint(algorithms);
            if (jkt.isPresent()) {
                return jkt;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Credential> fromCache(final Request request) {
        return Optional.ofNullable(tokenCache.get(request.uri()))
            .filter(credential -> credential.getExpiration().isAfter(Instant.now()));
    }

    @Override
    public CompletionStage<Optional<Credential>> authenticate(final Authenticator authenticator,
            final Request request, final Set<String> algorithms) {
        return authenticator.authenticate(this, request, algorithms)
            .thenApply(credential -> {
                if (credential != null) {
                    tokenCache.put(request.uri(), credential);
                }
                return Optional.ofNullable(credential);
            });
    }

    @Override
    public CompletionStage<Optional<Credential>> authenticate(final Request request,
            final Set<String> algorithms) {
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
