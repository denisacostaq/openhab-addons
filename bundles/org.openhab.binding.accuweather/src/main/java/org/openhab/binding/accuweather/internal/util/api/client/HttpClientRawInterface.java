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

package org.openhab.binding.accuweather.internal.util.api.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.exceptions.HttpErrorResponseException;

/**
 * The {@link HttpClientRawInterface} is responsible for making http requests to accuweather.com.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public interface HttpClientRawInterface<V, E extends Throwable> {
    V getAdminAreas(String countryDomainName, String apiKey) throws HttpErrorResponseException, E;

    V citySearch(String countryDomainName, String adminCodeId, String cityNameQuery, String apiKey)
            throws HttpErrorResponseException, E;

    V neighborsCities(String cityKey, String apiKey) throws HttpErrorResponseException, E;

    V getCurrentConditions(String cityKey, String apiKey) throws HttpErrorResponseException, E;

    V geoPositionSearch(@Nullable Float latitude, @Nullable Float longitude, String apiKey)
            throws HttpErrorResponseException, E;
}
