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

import java.util.List;

import org.openhab.binding.accuweather.internal.handler.AccuweatherBridgeHandler;
import org.openhab.binding.accuweather.internal.model.pojo.AdministrativeArea;
import org.openhab.binding.accuweather.internal.model.pojo.CitySearchResult;
import org.openhab.binding.accuweather.internal.model.pojo.CurrentConditions;
import org.openhab.binding.accuweather.internal.model.pojo.ErrorResponse;

/**
 * The {@link AccuweatherBridgeHandler} is responsible for deserializing responses from accuweather.com
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public interface ObjectMapper {
    List<CitySearchResult> deserializeCitySearchResult(String json);

    CitySearchResult deserializeSingleCitySearchResult(String json);

    List<AdministrativeArea> deserializeAdminAreasResult(String json);

    CurrentConditions deserializeCurrentConditions(String json);

    ErrorResponse deserializeErrorResponse(String json);

    boolean canDeserializeContentType(String ct);

    // TODO(denisacostaq@gmail.com): remove
    String getCityKey(String json);

    // TODO(denisacostaq@gmail.com): remove
    Double getTemperature(String json);

    // TODO(denisacostaq@gmail.com): remove
    Boolean hasPrecipitation(String json);
}
