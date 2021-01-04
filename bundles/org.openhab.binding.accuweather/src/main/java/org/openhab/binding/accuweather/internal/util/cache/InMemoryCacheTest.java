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

package org.openhab.binding.accuweather.internal.util.cache;

//import static org.junit.jupiter.api.Assertions.*;

import org.openhab.binding.accuweather.internal.interfaces.Cache;

/**
 * The {@link InMemoryCacheTest} have unit test implementations for InMemoryCache class.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
class InMemoryCacheTest {
    Cache<Integer> cache = new InMemoryCache<>();

    // @org.junit.jupiter.api.BeforeEach
    // void setUp() {
    // for (int i = 0; i < 100; i++) {
    // cache.setValue(String.valueOf(i), i * 2);
    // }
    // }
    //
    // @org.junit.jupiter.api.AfterEach
    // void tearDown() {
    // cache.invalidateCache();
    // }
    //
    // @org.junit.jupiter.api.Test
    // void getValue() {
    // // TODO(denisacostaq@gmail.com): todo
    // "2".equals(cache.getValue("1"));
    // }
    //
    // @org.junit.jupiter.api.Test
    // void setValue() {
    // // TODO(denisacostaq@gmail.com): todo
    // }
    //
    // @org.junit.jupiter.api.Test
    // void invalidateValue() {
    // // TODO(denisacostaq@gmail.com): todo
    // }
    //
    // @org.junit.jupiter.api.Test
    // void invalidateCache() {
    // // TODO(denisacostaq@gmail.com): todo
    // }
}
