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
package com.inrupt.client.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.inrupt.client.ClientCache;
import com.inrupt.client.spi.CacheBuilderService;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A {@link CacheBuilderService} using a Guava-based cache.
 */
public class GuavaCacheBuilder implements CacheBuilderService {

    @Override
    public <T, U> ClientCache<T, U> build(final int maximumSize, final Duration duration) {
        return ofCache(CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(duration.getSeconds(), TimeUnit.SECONDS)
                .build());
    }

    /**
     * Create a {@link ClientCache} directly from an existing Guava {@link Cache}.
     *
     * @param cache the pre-built cache
     * @param <T> the key type
     * @param <U> the value type
     * @return a cache suitable for use in the Inrupt Client libraries
     */
    public static <T, U> ClientCache<T, U> ofCache(final Cache<T, U> cache) {
        return new GuavaCache<>(cache);
    }
}


