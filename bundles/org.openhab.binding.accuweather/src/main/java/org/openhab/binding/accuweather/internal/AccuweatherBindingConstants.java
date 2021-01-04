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
package org.openhab.binding.accuweather.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AccuweatherBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class AccuweatherBindingConstants {

    private static final String BINDING_ID = "accuweather";

    // List of all Thing Type UIDs
    private static final String THING_TYPE_BRIDGE = "bridge";
    public static final ThingTypeUID UID_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_BRIDGE);
    private static final String THING_TYPE_STATION = "station";
    public static final ThingTypeUID UID_STATION = new ThingTypeUID(BINDING_ID, THING_TYPE_STATION);

    // List of all Channel ids
    public static final String CH_TEMPERATURE = "temperature";
}
