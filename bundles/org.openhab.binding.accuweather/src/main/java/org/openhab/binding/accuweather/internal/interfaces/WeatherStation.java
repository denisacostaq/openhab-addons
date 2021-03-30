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

package org.openhab.binding.accuweather.internal.interfaces;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;

/**
 * The {@link WeatherStation} is the specification to get weather condition values from a station
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public interface WeatherStation<HttpRespT, CacheValT, E extends Throwable> {
    @Nullable
    CurrentConditions currentConditions() throws E;

    boolean verifyStationConfigParams(String countryCode, Integer adminCode, String cityName) throws E;
}
