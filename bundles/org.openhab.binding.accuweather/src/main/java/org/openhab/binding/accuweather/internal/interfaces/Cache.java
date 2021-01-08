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

package org.openhab.binding.accuweather.internal.interfaces;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Cache} is the specification to keep values.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public interface Cache<T> {

    /**
     *
     * @param key
     * @return the value matching with the key or null if any
     */
    // FIXME(denisacostaq@gmail.com): Add a max age here
    T getValue(String key);

    /**
     * store a value indexed with the giving key
     * 
     * @param key
     * @param obj
     */
    void setValue(String key, T obj);

    /**
     * set the value indexed with key as invalid
     * 
     * @param key
     */
    void invalidateValue(String key);

    /**
     * invalidate all the values of the cache
     */
    void invalidateCache();
}
