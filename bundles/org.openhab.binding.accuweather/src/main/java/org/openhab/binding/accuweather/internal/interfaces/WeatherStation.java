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

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link WeatherStation} is the specification to get weather condition values from a station
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public interface WeatherStation<HttpRespT, CacheValT, E extends Throwable> {
    /**
     * @return the temperature in the station
     */
    // FIXME(denisacostaq@gmail.com): should include scale, like for example celcious, fahrenheit
    @Nullable
    Float getTemperature() throws E;

    /**
     * @return the humidity in the station
     */
    @Nullable
    Float getHumidity();

    /**
     * @return the current time in the station
     */
    @Nullable
    Date getCurrentTime() throws E;

    @Nullable
    Boolean hasPrecipitation() throws E;

    String getPrecipitationType() throws E;

    String getWeatherText() throws E;

    @Nullable
    Integer getWeatherIcon() throws E;

    boolean verifyStationConfigParams(String countryCode, Integer adminCode, String cityName) throws E;
}
