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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.accuweather.internal.interfaces.Cache;

/**
 * The {@link InMemoryCache} is a simple in memory implementation of the {@link Cache} based in
 * a HasMap. This implementation is safe to use in concurrency conditions.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class InMemoryCache<T> implements Cache<T> {
    Map<String, T> data = new HashMap<>();
    ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public T getValue(String key) {
        lock.readLock().lock();
        T t = data.get(key);
        lock.readLock().unlock();
        return t;
    }

    @Override
    public void setValue(String key, T obj) {
        lock.writeLock().lock();
        data.put(key, obj);
        lock.writeLock().unlock();
    }

    @Override
    // FIXME(denisacostaq@gmail.com): Invalidate key
    public void invalidateValue(String key) {
        lock.writeLock().lock();
        data.remove(key);
        lock.writeLock().unlock();
    }

    @Override
    public void invalidateCache() {
        lock.writeLock().lock();
        data.keySet().stream().collect(Collectors.toSet()).stream().forEach(key -> invalidateValue(key));
        lock.writeLock().unlock();
    }
}
