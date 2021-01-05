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

import java.io.IOException;

import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HttpClient} is responsible for making call to the accuweather endpoints.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class HttpClient implements HttpClientRawInterface {
    private final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    // Timeout of the call to the Ambient Weather devices API
    public static final int DEVICES_API_TIMEOUT = 20000;

    // Time to wait after failed key validation
    public static final long KEY_VALIDATION_DELAY = 60L;

    private String makeHttpRequest(String url) {
        try {
            String response = HttpUtil.executeUrl("GET", url, DEVICES_API_TIMEOUT);
            logger.trace("Http: Response = {}", response);
            return response;
        } catch (IOException e) {
            // executeUrl throws IOException when it gets a Not Authorized (401) response
            logger.debug("Bridge: Got IOException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Invalid API or application
            // key");
            // rescheduleValidateKeysJob();
        } catch (IllegalArgumentException e) {
            logger.debug("Bridge: Got IllegalArgumentException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Unable to get devices");
            // rescheduleValidateKeysJob();
        }
        return "";
    }

    /**
     *
     * @return the city key
     */
    @Override
    public String citySearch(String countryDomainName, String adminCodeId, String cityNameQuery, String apiKey) {
        final String CITY_SEARCH_URL = "http://dataservice.accuweather.com/locations/v1/cities/%COUNTRY_DOMAIN_NAME_CODE%/%ADMIN_CODE%/search?apikey=%API_KEY%&q=%CITY_NAME_QUERY%";
        String url = CITY_SEARCH_URL.replace("%COUNTRY_DOMAIN_NAME_CODE%", countryDomainName)
                .replace("%ADMIN_CODE%", adminCodeId).replace("%API_KEY%", apiKey)
                .replace("%CITY_NAME_QUERY%", cityNameQuery);
        logger.debug("Validating API key through getting cities API");
        // FIXME(denisacostaq@gmail.com): Use the build url instead
        url = "http://localhost:8000/City_Search_results_narrowed_by_countryCode_and_adminCode_.json";
        logger.debug("Bridge: Querying City Search (results narrowed by countryCode and adminCode Accuweather service");
        return makeHttpRequest(url);
    }

    @Override
    public String getCurrentConditions(String cityKey, String apiKey) {
        final String CURRENT_CONDITIONS_URL = "http://dataservice.accuweather.com/currentconditions/v1/%CITY_KEY%?apikey=%API_KEY%";
        String url = CURRENT_CONDITIONS_URL.replace("%CITY_KEY%", cityKey).replace("%API_KEY%", apiKey);
        // FIXME(denisacostaq@gmail.com): Use the builded url instead
        url = "http://localhost:8000/Current_Conditions.json";
        logger.debug("Bridge: Getting current conditions");
        return makeHttpRequest(url);
    }
}
