/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package tests.org.openhab.binding.accuweather.internal.util.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.*;
import org.openhab.binding.accuweather.internal.interfaces.Cache;
import org.openhab.binding.accuweather.internal.util.cache.InMemoryCache;

/**
 * The {@link InMemoryCacheTest} have the unit tests for {@link InMemoryCache}.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
class InMemoryCacheTest {
    private static List<Integer> vals;
    private static final int length = new Random().nextInt(100) + 1;

    @BeforeAll
    static void setUp() {
        vals = new ArrayList<>(length);
        Random rdn = new Random();
        for (int i = 0; i < length; i++) {
            vals.add(rdn.nextInt(length));
        }
    }

    @AfterAll
    static void tearDown() {
        vals.clear();
    }

    @Test
    void getValue() {
        // Giving
        final Cache<Integer> cache = new InMemoryCache<>(vals.size());
        for (int i = 0; i < vals.size(); i++) {
            cache.setValue(String.valueOf(i), vals.get(i));
        }

        // When
        List<Integer> retrievedVals = new ArrayList<>(vals.size());
        for (int idx = 0; idx < vals.size(); idx++) {
            retrievedVals.add(cache.getValue(String.valueOf(idx)));
        }

        // Then
        for (int idx = 0; idx < vals.size(); idx++) {
            assertThat(retrievedVals.get(idx), is(equalTo(vals.get(idx))));
        }
        retrievedVals.clear();
        cache.invalidateCache();
    }

    @Test
    void setValue() {
        // Giving
        final Cache<Cache> cache = new InMemoryCache<>(1);
        cache.setValue("cache", cache);

        // When
        Cache retrieved = cache.getValue("cache");

        // Then
        assertThat(retrieved, is(equalTo(cache)));
        cache.invalidateCache();
    }

    @Test
    void invalidateValue() {
        // Giving
        final Cache<Integer> cache = new InMemoryCache<>(vals.size());
        final List<Boolean> invalidated = new ArrayList<>(vals.size());
        Random rdn = new Random();
        for (int i = 0; i < vals.size(); i++) {
            cache.setValue(String.valueOf(i), vals.get(i));
            invalidated.add(rdn.nextBoolean());
        }

        // When
        for (int idx = 0; idx < vals.size(); idx++) {
            if (invalidated.get(idx)) {
                cache.invalidateValue(String.valueOf(idx));
            }
        }

        // Then
        for (int idx = 0; idx < vals.size(); idx++) {
            Integer retrieved = cache.getValue(String.valueOf(idx));
            if (invalidated.get(idx)) {
                assertThat(retrieved, is(nullValue()));
            } else {
                assertThat(retrieved.intValue(), is(equalTo(vals.get(idx))));
            }
        }
        cache.invalidateCache();
    }

    @Test
    void invalidateCache() {
        // Giving
        final Cache<Integer> cache = new InMemoryCache<>(vals.size());
        for (int i = 0; i < vals.size(); i++) {
            cache.setValue(String.valueOf(i), vals.get(i));
        }

        // When
        cache.invalidateCache();

        // Then
        for (int idx = 0; idx < vals.size(); idx++) {
            assertThat(cache.getValue(String.valueOf(idx)), is(nullValue()));
        }
    }
}
