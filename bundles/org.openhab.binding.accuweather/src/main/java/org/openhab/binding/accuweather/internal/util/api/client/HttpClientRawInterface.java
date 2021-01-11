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

package org.openhab.binding.accuweather.internal.util.api.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.accuweather.internal.exceptions.HttpErrorResponseException;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;

/**
 * The {@link HttpClientRawInterface} is responsible for making http requests to accuweather.com.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public interface HttpClientRawInterface {
    String getAdminAreas(String countryDomainName, String apiKey)
            throws HttpErrorResponseException, RemoteErrorResponseException;

    String citySearch(String countryDomainName, String adminCodeId, String cityNameQuery, String apiKey)
            throws HttpErrorResponseException, RemoteErrorResponseException;

    String neighborsCities(String cityKey, String apiKey)
            throws HttpErrorResponseException, RemoteErrorResponseException;

    String getCurrentConditions(String cityKey, String apiKey)
            throws HttpErrorResponseException, RemoteErrorResponseException;

    String geoPositionSearch(Float latitude, Float longitude, String apiKey)
            throws HttpErrorResponseException, RemoteErrorResponseException;
}
