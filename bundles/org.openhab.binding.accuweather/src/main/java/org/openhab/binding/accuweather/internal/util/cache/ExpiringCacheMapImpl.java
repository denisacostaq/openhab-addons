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

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringCache;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringCacheMapInterface;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringValue;
import org.openhab.binding.accuweather.internal.interfaces.cache.ThrowingSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ExpiringCache} is TODO(denisacostaq@gmail.com): todo
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class ExpiringCacheMapImpl<K, V, E extends Throwable> implements ExpiringCacheMapInterface<K, V, E> {
    private final Logger logger = LoggerFactory.getLogger(ExpiringCacheMapImpl.class);
    private final ConcurrentMap<K, ExpiringCache<@Nullable V, E>> items = new ConcurrentHashMap<>();

    /**
     * Creates an {@link ExpiringCache} and adds it to the cache.
     *
     * @param key the key with which the specified value is to be associated
     * @param action the action for the item to be associated with the specified key to retrieve/calculate the value
     */
    @Override
    public void put(K key, ThrowingSupplier<@Nullable ExpiringValue<V>, E> action) {
        items.put(key, new ExpiringCacheImpl<V, E>(action));
    }

    /**
     * If the specified key is not already associated, associate it with the given action.
     *
     * Note that this method has the overhead of actually calling/performing the action
     *
     * @param key the key with which the specified value is to be associated
     * @param action the action for the item to be associated with the specified key to retrieve/calculate the value
     * @return the (cached) value for the specified key
     */
    @Override
    public @Nullable V putIfAbsentAndGet(K key, ThrowingSupplier<@Nullable ExpiringValue<V>, E> action) throws E {
        if (Objects.isNull(key)) {
            throw new IllegalArgumentException("Item cannot be added as key is null.");
        }
        items.putIfAbsent(key, new ExpiringCacheImpl<@Nullable V, E>(action));
        return get(key);
    }

    /**
     * Returns the value associated with the given key - possibly from the cache, if it is still valid.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the given key, or null if there is no cached value for the given key
     */
    @Override
    public @Nullable V get(K key) throws E {
        final ExpiringCache<@Nullable V, E> item = items.get(key);
        if (Objects.isNull(item)) {
            logger.debug("No item for key '{}' found", key);
            return null;
        } else {
            return item.getValue();
        }
    }

    /**
     * Invalidates the value associated with the given key in the cache.
     *
     * @param key the key whose associated value is to be invalidated
     */
    @Override
    public synchronized void invalidate(K key) {
        final ExpiringCache<@Nullable V, E> item = items.get(key);
        if (item == null) {
            logger.debug("No item for key '{}' found", key);
        } else {
            item.invalidateValue();
        }
    }

    /**
     * Invalidates all values in the cache.
     */
    @Override
    public synchronized void invalidateAll() {
        items.keySet().forEach(key -> this.invalidate(key));
    }

    @Override
    public synchronized void clear() {
        this.invalidateAll();
        items.keySet().stream().forEach(key -> {
            this.items.remove(key);
        });
    }
}
