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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TemperatureSummary} is used to map the json response from accuwater
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class TemperatureSummary {
    @SerializedName(value = "Past6HourRange")
    public Past6HourRange past6HourRange;
    @SerializedName(value = "Past12HourRange")
    public Past12HourRange past12HourRange;
    @SerializedName(value = "Past24HourRange")
    public Past24HourRange past24HourRange;
}
