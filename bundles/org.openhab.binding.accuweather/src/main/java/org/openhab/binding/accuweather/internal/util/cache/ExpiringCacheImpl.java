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

import java.lang.ref.SoftReference;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringCache;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringValue;
import org.openhab.binding.accuweather.internal.interfaces.cache.ThrowingSupplier;

/**
 * The {@link ExpiringCache} is TODO(denisacostaq@gmail.com): todo
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class ExpiringCacheImpl<V, E extends Throwable> implements ExpiringCache<V, E> {
    private final ThrowingSupplier<@Nullable ExpiringValue<V>, E> action;
    private SoftReference<@Nullable ExpiringValue<V>> value = new SoftReference<@Nullable ExpiringValue<V>>(null);

    /**
     * Create a new instance.
     *
     * @param action the action to retrieve/calculate the value
     * @throws IllegalArgumentException For an expire value <=0.
     */
    public ExpiringCacheImpl(ThrowingSupplier<@Nullable ExpiringValue<V>, E> action) {
        this.action = action;
    }

    /**
     * Returns the value - possibly from the cache, if it is still valid.
     */
    @Override
    public synchronized @Nullable V getValue() throws E {
        ExpiringValue<V> cachedValue = value.get();
        if (Objects.isNull(cachedValue) || isExpired()) {
            cachedValue = refreshValue().get();
        }
        return cachedValue.value();
    }

    /**
     * Invalidates the value in the cache.
     */
    @Override
    public final synchronized void invalidateValue() {
        value = new SoftReference<>(null);
    }

    /**
     * Refreshes and returns the value in the cache.
     *
     * @return the new value
     */
    @Override
    public synchronized SoftReference<@Nullable ExpiringValue<V>> refreshValue() throws E {
        ExpiringValue<V> freshValue = action.get();
        value = new SoftReference<>(freshValue);
        return value;
    }

    /**
     * Checks if the value is expired.
     *
     * @return true if the value is expired
     */
    @Override
    public boolean isExpired() {
        return Objects.isNull(value.get()) || !value.get().stillValid(new Date(Instant.now().toEpochMilli()));
    }
}
