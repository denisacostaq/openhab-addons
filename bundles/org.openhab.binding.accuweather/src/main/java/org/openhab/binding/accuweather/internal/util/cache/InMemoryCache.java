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

import org.openhab.binding.accuweather.internal.interfaces.Cache;

/**
 * The {@link InMemoryCache} is a simple in memory implementation of the {@link Cache} based in
 * a HasMap.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class InMemoryCache<T> implements Cache<T> {
    Map<String, T> data = new HashMap<>();

    @Override
    public T getValue(String key) {
        return data.get(key);
    }

    @Override
    public void setValue(String key, T obj) {
        data.put(key, obj);
    }

    @Override
    // FIXME(denisacostaq@gmail.com): Invalidate key
    public void invalidateValue(String key) {
        data.remove(key);
    }

    @Override
    public void invalidateCache() {
        for (String key : data.keySet()) {
            invalidateValue(key);
        }
    }
}
