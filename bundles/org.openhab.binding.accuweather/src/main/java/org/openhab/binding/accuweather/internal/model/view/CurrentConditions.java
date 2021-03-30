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

package org.openhab.binding.accuweather.internal.model.view;

import java.util.Date;

/**
 * The {@link CurrentConditions} is used to map the json response from accuwater
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class CurrentConditions {
    private Date localObservationDateTime;
    private String weatherText;
    private Integer weatherIcon;
    private boolean hasPrecipitation;
    private String precipitationType;
    // FIXME(denisacostaq@gmail.com): should include scale, like for example celcious, fahrenheit
    private Float temperature;
    private Float realFeelTemperature;
    private Float realFeelTemperatureShade;
    private int relativeHumidity;
    private int indoorRelativeHumidity;
    private int uVIndex;
    private String uVIndexText;
    private String obstructionsToVisibility;
    private int cloudCover;

    public Date getLocalObservationDateTime() {
        return localObservationDateTime;
    }

    public void setLocalObservationDateTime(Date localObservationDateTime) {
        this.localObservationDateTime = localObservationDateTime;
    }

    public String getWeatherText() {
        return weatherText;
    }

    public void setWeatherText(String weatherText) {
        this.weatherText = weatherText;
    }

    public Integer getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(Integer weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public boolean isHasPrecipitation() {
        return hasPrecipitation;
    }

    public void setHasPrecipitation(boolean hasPrecipitation) {
        this.hasPrecipitation = hasPrecipitation;
    }

    public String getPrecipitationType() {
        return precipitationType;
    }

    public void setPrecipitationType(String precipitationType) {
        this.precipitationType = precipitationType;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getRealFeelTemperature() {
        return realFeelTemperature;
    }

    public void setRealFeelTemperature(Float realFeelTemperature) {
        this.realFeelTemperature = realFeelTemperature;
    }

    public Float getRealFeelTemperatureShade() {
        return realFeelTemperatureShade;
    }

    public void setRealFeelTemperatureShade(Float realFeelTemperatureShade) {
        this.realFeelTemperatureShade = realFeelTemperatureShade;
    }

    public int getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(int relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public int getIndoorRelativeHumidity() {
        return indoorRelativeHumidity;
    }

    public void setIndoorRelativeHumidity(int indoorRelativeHumidity) {
        this.indoorRelativeHumidity = indoorRelativeHumidity;
    }

    public int getuVIndex() {
        return uVIndex;
    }

    public void setuVIndex(int uVIndex) {
        this.uVIndex = uVIndex;
    }

    public String getuVIndexText() {
        return uVIndexText;
    }

    public void setuVIndexText(String uVIndexText) {
        this.uVIndexText = uVIndexText;
    }

    public String getObstructionsToVisibility() {
        return obstructionsToVisibility;
    }

    public void setObstructionsToVisibility(String obstructionsToVisibility) {
        this.obstructionsToVisibility = obstructionsToVisibility;
    }

    public int getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(int cloudCover) {
        this.cloudCover = cloudCover;
    }
}
