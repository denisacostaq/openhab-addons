/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.accuweather.internal.util.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringCache;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringValue;
import org.openhab.binding.accuweather.internal.interfaces.cache.ThrowingSupplier;

import generator.Generator;

/**
 * The {@link ExpiringCacheMapImplTest} have the unit tests for {@link ExpiringCacheMapImpl}.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
class ExpiringCacheMapImplTest {
    @Mock
    ExpiringValue<Object> expiringValue;
    @Mock
    ThrowingSupplier<@Nullable ExpiringValue<Object>, @NonNull RemoteErrorResponseException> supplier;
    @Mock
    ExpiringCache<@Nullable ExpiringValue<Object>, @NonNull RemoteErrorResponseException> expiringCache;
    private ExpiringCacheMapImpl<String, Object, @NonNull RemoteErrorResponseException> expiringCacheMap;

    private void initMocks() {
        expiringValue = Mockito.mock(ExpiringValue.class);
        supplier = Mockito.mock(ThrowingSupplier.class);
        expiringCache = Mockito.mock(ExpiringCache.class);
    }

    @BeforeEach
    void setUp() {
        initMocks();
        expiringCacheMap = new ExpiringCacheMapImpl<>();
    }

    @AfterEach
    void tearDown() {
        expiringCacheMap.clear();
    }

    @Test
    void put() throws RemoteErrorResponseException {
        // Giving
        String key = Generator.getRandomString();
        final String val = Generator.getRandomString();
        Mockito.when(supplier.get()).thenReturn(new ExpiringValueImpl<>(null, val));
        assertThat(expiringCacheMap.get(key), is(nullValue()));

        // When
        expiringCacheMap.put(key, supplier);

        // Then
        assertThat(expiringCacheMap.get(key), is(notNullValue()));
        assertThat(expiringCacheMap.get(key), is(equalTo(val)));
    }

    @Test
    void putIfAbsentAndGet() throws RemoteErrorResponseException {
        // Giving
        final String key = Generator.getRandomString();
        final String val = Generator.getRandomString();
        Mockito.when(supplier.get()).thenReturn(new ExpiringValueImpl<>(null, val));

        // When
        Object result = expiringCacheMap.putIfAbsentAndGet(key, supplier);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(val)));
    }

    @Test
    void putIfAbsentAndGetNoAbsent() throws RemoteErrorResponseException {
        // Giving
        final String key = Generator.getRandomString();
        final String val = Generator.getRandomString();
        Mockito.when(supplier.get()).thenReturn(new ExpiringValueImpl<>(null, val));
        expiringCacheMap.put(key, supplier);
        final String val2 = Generator.getRandomString();
        ThrowingSupplier<@Nullable ExpiringValue<Object>, @NonNull RemoteErrorResponseException> supplier2 = Mockito
                .mock(ThrowingSupplier.class);
        Mockito.when(supplier2.get()).thenReturn(new ExpiringValueImpl<>(null, val2));

        // When
        Object result = expiringCacheMap.putIfAbsentAndGet(key, supplier);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(val)));
    }

    @Test
    void get() throws RemoteErrorResponseException {
        // Giving
        final String key = Generator.getRandomString();
        final String val = Generator.getRandomString();
        Mockito.when(supplier.get()).thenReturn(new ExpiringValueImpl<>(null, val));
        expiringCacheMap.put(key, supplier);

        // When
        Object result = expiringCacheMap.get(key);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(val)));
    }

    @Test
    void invalidate() throws RemoteErrorResponseException {
        // Giving
        final String key = Generator.getRandomString();
        final String val = Generator.getRandomString();
        Mockito.when(supplier.get()).thenReturn(new ExpiringValueImpl<>(null, val));
        expiringCacheMap.put(key, supplier);

        // When
        expiringCacheMap.invalidate(key);

        // Then
        // FIXME(denisacostaq@gmail.com): Make sure that the item.invalidateValue(); has been called
        // is required to inject the cache
    }

    @Test
    void invalidateAll() throws RemoteErrorResponseException {
        // Giving
        final String key = Generator.getRandomString();
        final String val = Generator.getRandomString();
        Mockito.when(supplier.get()).thenReturn(new ExpiringValueImpl<>(null, val));
        expiringCacheMap.put(key, supplier);

        // When
        expiringCacheMap.invalidateAll();

        // Then
        // FIXME(denisacostaq@gmail.com): Make sure that all item.invalidateValue(); has been called
        // is required to inject the cache
    }

    @Test
    void clear() throws RemoteErrorResponseException {
        // Giving
        final String key = Generator.getRandomString();
        final String val = Generator.getRandomString();
        Mockito.when(supplier.get()).thenReturn(new ExpiringValueImpl<>(null, val));
        expiringCacheMap.put(key, supplier);

        // When
        expiringCacheMap.clear();
        Object result = expiringCacheMap.get(key);

        // Then
        assertThat(result, is(nullValue()));
    }
}
