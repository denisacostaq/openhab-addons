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
package org.openhab.binding.accuweather.internal.interfaces.cache;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ExpiringCache} is TODO(denisacostaq@gmail.com): todo
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public interface ExpiringCacheMapInterface<K, V, E extends Throwable> {
    void put(K key, ThrowingSupplier<@Nullable ExpiringValue<V>, E> action);

    @Nullable
    V putIfAbsentAndGet(K key, ThrowingSupplier<@Nullable ExpiringValue<V>, E> action) throws E;

    @Nullable
    V get(K key) throws E;

    void invalidate(K key);

    void invalidateAll();

    void clear();
}
