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
 * The {@link Country} is used to map the json response from accuwater
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class Country {
    @SerializedName(value = "ID")
    public String iD;
    public String localizedName;
    public String englishName;

    public Country() {
    }

    public Country(String iD) {
        this.iD = iD;
    }
}
