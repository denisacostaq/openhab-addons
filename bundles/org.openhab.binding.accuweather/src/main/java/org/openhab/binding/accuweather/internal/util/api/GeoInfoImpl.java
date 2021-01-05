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

package org.openhab.binding.accuweather.internal.util.api;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.accuweather.internal.interfaces.GeoInfo;

/**
 * The {@link GeoInfoImpl} is responsible getting relevant location information based in coordinates.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class GeoInfoImpl implements GeoInfo {
    @Override
    public String getCityName(BigDecimal latitude, BigDecimal longitude) {
        // FIXME(denisacostaq@gmail.com): Use an external API
        return "Sofia";
    }

    @Override
    public String getCountryName(BigDecimal latitude, BigDecimal longitude) {
        // FIXME(denisacostaq@gmail.com): Use an external API
        return "Bulgaria";
    }

    @Override
    public String getCountryDomainName(BigDecimal latitude, BigDecimal longitude) {
        // FIXME(denisacostaq@gmail.com): Use an external API
        return "bg";
    }
}
