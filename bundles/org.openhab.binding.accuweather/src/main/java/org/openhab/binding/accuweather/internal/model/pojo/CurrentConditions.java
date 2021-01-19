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

package org.openhab.binding.accuweather.internal.model.pojo;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link CurrentConditions} is used to map the json response from accuwater
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class CurrentConditions {
    // TODO(denisacostaq@gmail.com): remove annotation and check error handling
    @SerializedName(value = "LocalObservationDateTime")
    public Date localObservationDateTime;
    public int epochTime;
    public String weatherText;
    public int weatherIcon;
    @SerializedName(value = "HasPrecipitation")
    public boolean hasPrecipitation;
    @SerializedName(value = "PrecipitationType")
    public Object precipitationType;
    public boolean isDayTime;
    @SerializedName(value = "Temperature")
    public Temperature temperature;
    public String mobileLink;
    public String link;
}
