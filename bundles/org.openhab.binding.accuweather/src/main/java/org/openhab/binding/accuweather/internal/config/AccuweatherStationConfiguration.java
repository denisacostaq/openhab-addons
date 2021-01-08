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
package org.openhab.binding.accuweather.internal.config;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AccuweatherStationConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherStationConfiguration {
    public static final String countryCodeEntryNameInXml = "countryCode";
    public static final String adminCodeEntryNameInXml = "adminCode";
    public static final String locationNameEntryNameInXml = "locationName";

    /**
     * eg: us, bg, cu
     */
    public String countryCode = "";

    /**
     * administrative area code relative to the country
     */
    public Integer adminCode = 0;

    /**
     * eg: New York, Sofia, Varna
     */
    public String locationName = "";

    @Override
    public String toString() {
        return String.format("countryCode: %s, adminCode: %d, locationName: %s",
                StringUtils.isEmpty(countryCode) ? 0 : countryCode, adminCode == 0 ? "Null" : adminCode,
                StringUtils.isEmpty(locationName) ? "Null" : locationName);
    }
}
