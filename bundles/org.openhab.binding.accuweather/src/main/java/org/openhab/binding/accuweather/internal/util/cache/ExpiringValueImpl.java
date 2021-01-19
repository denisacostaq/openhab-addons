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

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringCache;
import org.openhab.binding.accuweather.internal.interfaces.cache.ExpiringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ExpiringCache} is TODO(denisacostaq@gmail.com): todo
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class ExpiringValueImpl<V> implements ExpiringValue<V> {
    private final Logger logger = LoggerFactory.getLogger(ExpiringValueImpl.class);
    private final @Nullable Date expiry;
    private final V value;

    /**
     *
     * @param expiry the for when the value will expiry
     * @param value the value to be stored
     * @throws IllegalArgumentException For an expire value <=0.
     */
    public ExpiringValueImpl(@Nullable Date expiry, V value) {
        this.expiry = expiry;
        this.value = value;
        Date now = new Date(Instant.now().toEpochMilli());
        if (!Objects.isNull(expiresAt()) && !this.stillValid(now)) {
            // throw new IllegalArgumentException("Expire time must be after now");
            logger.warn("throw new IllegalArgumentException(\"Expire time must be after now\");");
        }
    }

    @Override
    public @Nullable Date expiresAt() {
        return expiry;
    }

    @Override
    public boolean stillValid(Date relativeTo) {
        return !Objects.isNull(expiresAt()) && relativeTo.before(expiresAt());
    }

    @Override
    public V value() {
        return value;
    }
}
