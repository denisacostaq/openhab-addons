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
    // https://json2csharp.com/json-to-pojo
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
    @SerializedName(value = "RealFeelTemperature")
    public RealFeelTemperature realFeelTemperature;
    @SerializedName(value = "RealFeelTemperatureShade")
    public RealFeelTemperatureShade realFeelTemperatureShade;
    @SerializedName(value = "RelativeHumidity")
    public int relativeHumidity;
    @SerializedName(value = "IndoorRelativeHumidity")
    public int indoorRelativeHumidity;
    @SerializedName(value = "DewPoint")
    public DewPoint dewPoint;
    @SerializedName(value = "Wind")
    public Wind wind;
    @SerializedName(value = "WindGust")
    public WindGust windGust;
    @SerializedName(value = "UVIndex")
    public int uVIndex;
    @SerializedName(value = "UVIndexText")
    public String uVIndexText;
    @SerializedName(value = "Visibility")
    public Visibility visibility;
    @SerializedName(value = "ObstructionsToVisibility")
    public String obstructionsToVisibility;
    @SerializedName(value = "CloudCover")
    public int cloudCover;
    @SerializedName(value = "Ceiling")
    public Ceiling ceiling;
    @SerializedName(value = "Pressure")
    public Pressure pressure;
    @SerializedName(value = "PressureTendency")
    public PressureTendency pressureTendency;
    @SerializedName(value = "Past24HourTemperatureDeparture")
    public Past24HourTemperatureDeparture past24HourTemperatureDeparture;
    @SerializedName(value = "ApparentTemperature")
    public ApparentTemperature apparentTemperature;
    @SerializedName(value = "WindChillTemperature")
    public WindChillTemperature windChillTemperature;
    @SerializedName(value = "WetBulbTemperature")
    public WetBulbTemperature wetBulbTemperature;
    @SerializedName(value = "Precip1hr")
    public Precip1hr precip1hr;
    @SerializedName(value = "PrecipitationSummary")
    public PrecipitationSummary precipitationSummary;
    @SerializedName(value = "TemperatureSummary")
    public TemperatureSummary temperatureSummary;
    @SerializedName(value = "MobileLink")
    public String mobileLink;
    public String link;

    public CurrentConditions() {
    }

    public CurrentConditions(String weatherText, boolean isDayTime, Temperature temperature) {
        this.weatherText = weatherText;
        this.isDayTime = isDayTime;
        this.temperature = temperature;
    }

    public CurrentConditions(Date localObservationDateTime, String weatherText, boolean hasPrecipitation,
            Object precipitationType, boolean isDayTime, Temperature temperature) {
        this.localObservationDateTime = localObservationDateTime;
        this.weatherText = weatherText;
        this.hasPrecipitation = hasPrecipitation;
        this.precipitationType = precipitationType;
        this.isDayTime = isDayTime;
        this.temperature = temperature;
    }
}
