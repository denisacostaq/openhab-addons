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

package org.openhab.binding.accuweather.internal.model.pojo;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link CitySearchResult} is used to map the json response from accuwater
 * https://developer.accuweather.com/accuweather-locations-api/apis/get/locations/v1/cities/%7BcountryCode%7D/%7BadminCode%7D/search
 * 
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class CitySearchResult {
    public int version;
    @SerializedName(value = "Key")
    public String key;
    public String type;
    public int rank;
    public String localizedName;
    public String englishName;
    public String primaryPostalCode;
    public Region region;
    public Country country;
    public AdministrativeArea administrativeArea;
    public TimeZone timeZone;
    public GeoPosition geoPosition;
    public boolean isAlias;
    public List<Object> supplementalAdminAreas;
    public List<String> dataSets;
}
