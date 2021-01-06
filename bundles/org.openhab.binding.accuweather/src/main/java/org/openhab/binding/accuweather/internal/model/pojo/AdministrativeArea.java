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

/**
 * The {@link AdministrativeArea} is used to map the json response from accuwater
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class AdministrativeArea {
    public String iD;
    public String localizedName;
    public String englishName;
    public int level;
    public String localizedType;
    public String englishType;
    public String countryID;

    public AdministrativeArea() {
    }

    public AdministrativeArea(String iD, String countryID) {
        this.iD = iD;
        this.countryID = countryID;
    }
}
