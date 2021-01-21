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
 * The {@link ExpiringCacheImplTest} have the unit tests for {@link ExpiringCacheImpl}.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
class ExpiringCacheImplTest {
    @Mock
    ThrowingSupplier<@Nullable ExpiringValue<Object>, @NonNull RemoteErrorResponseException> supplier;
    @Mock
    ExpiringValue<Object> expiringValue;
    private ExpiringCache<Object, @NonNull RemoteErrorResponseException> expiringCache = null;

    private void initMocks() {
        supplier = Mockito.mock(ThrowingSupplier.class);
        expiringValue = Mockito.mock(ExpiringValue.class);
    }

    @BeforeEach
    public void beforeEach() {
        initMocks();
        expiringCache = new ExpiringCacheImpl<>(supplier);
    }

    @Test
    void getValue() throws Throwable {
        // Giving
        final String val = Generator.getRandomString();
        Mockito.when(expiringValue.stillValid(Mockito.any())).thenReturn(true);
        Mockito.when(expiringValue.value()).thenReturn(val);
        Mockito.when(supplier.get()).thenReturn(expiringValue);

        // When
        Object result = expiringCache.getValue();

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(val)));
    }

    @Test
    void refreshValue() throws Throwable {
        // Giving

        // When
        Object result = expiringCache.refreshValue();

        // Then
        Mockito.verify(supplier, Mockito.times(1)).get();
    }

    @Test
    void isExpiredForNullValue() {
        // Giving
        Mockito.when(expiringValue.stillValid(Mockito.any())).thenReturn(true);

        // When
        boolean result = expiringCache.isExpired();

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(true)));
    }

    @Test
    void isExpired() throws Throwable {
        // Giving
        boolean val = Generator.getRandomBool();
        Mockito.when(expiringValue.stillValid(Mockito.any())).thenReturn(val);
        Mockito.when(supplier.get()).thenReturn(expiringValue);
        expiringCache.refreshValue();

        // When
        boolean result = expiringCache.isExpired();

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(equalTo(!val)));
    }
}
