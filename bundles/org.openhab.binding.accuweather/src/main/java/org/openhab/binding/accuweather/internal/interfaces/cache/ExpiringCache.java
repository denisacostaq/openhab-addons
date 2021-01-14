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

package org.openhab.binding.accuweather.internal.interfaces.cache;

import java.lang.ref.SoftReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ExpiringCache} is TODO(denisacostaq@gmail.com): todo
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public interface ExpiringCache<V, E extends Throwable> {
    @Nullable
    V getValue() throws E;

    void invalidateValue();

    SoftReference<@Nullable ExpiringValue<V>> refreshValue() throws E;

    boolean isExpired();
}
