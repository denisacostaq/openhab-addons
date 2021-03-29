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
 * The {@link PrecipitationSummary} is used to map the json response from accuwater
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class PrecipitationSummary {
    @SerializedName(value = "Precipitation")
    public Precipitation precipitation;
    @SerializedName(value = "PastHour")
    public PastHour pastHour;
    @SerializedName(value = "Past3Hours")
    public Past3Hours past3Hours;
    @SerializedName(value = "Past6Hours")
    public Past6Hours past6Hours;
    @SerializedName(value = "Past9Hours")
    public Past9Hours past9Hours;
    @SerializedName(value = "Past12Hours")
    public Past12Hours past12Hours;
    @SerializedName(value = "Past18Hours")
    public Past18Hours past18Hours;
    @SerializedName(value = "Past24Hours")
    public Past24Hours past24Hours;
}
