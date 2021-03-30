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

package org.openhab.binding.accuweather.internal.model.mapper;

import java.util.Date;
import java.util.Objects;
import java.util.Random;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.model.view.CurrentConditions;

/**
 * The {@link ModelTranslator} is used to map the json response from accuwater
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class ModelTranslator {
    public static CurrentConditions currentConditions(
            org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions pojo) {
        CurrentConditions view = new CurrentConditions();
        view.setTemperature(getTemperature(pojo));
        view.setLocalObservationDateTime(getCurrentTime(pojo));
        view.setPrecipitationType(getPrecipitationType(pojo));
        view.setWeatherText(getWeatherText(pojo));
        view.setWeatherIcon(getWeatherIcon(pojo));
        return view;
    }

    private static Float getTemperature(
            org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions currentConditions) {
        if (Objects.isNull(currentConditions) || currentConditions.temperature == null
                || Objects.isNull(currentConditions.temperature.metric)) {
            return null;
        }
        return Double.valueOf(currentConditions.temperature.metric.value).floatValue();
    }

    private static Date getCurrentTime(
            org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions currentConditions) {
        return Objects.isNull(currentConditions) ? null : currentConditions.localObservationDateTime;
    }

    public static Float getHumidity(
            org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions currentConditions) {
        return null;
    }

    private static Boolean hasPrecipitation(
            org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions currentConditions) {
        return currentConditions.hasPrecipitation;
    }

    public static String getPrecipitationType(
            org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions currentConditions) {
        if (hasPrecipitation(currentConditions)) {
            return currentConditions.precipitationType.toString();
        } else {
            return "None"; // TODO(denisacostaq@gmail.com): named var
        }
    }

    public static String getWeatherText(
            org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions currentConditions) {
        return currentConditions.weatherText;
    }

    public @Nullable static Integer getWeatherIcon(
            org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions currentConditions) {
        Integer weatherIcon = currentConditions.weatherIcon;
        weatherIcon = new Random().nextInt(8) + 1;
        if (weatherIcon == 3) {
            weatherIcon = null;
        }
        return weatherIcon;
    }
}
