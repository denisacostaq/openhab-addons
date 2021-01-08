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

import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.accuweather.internal.exceptions.HttpErrorResponseException;
import org.openhab.binding.accuweather.internal.exceptions.RemoteErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HttpClient} is responsible for making call to the accuweather endpoints.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class HttpClient implements HttpClientRawInterface {
    private final Logger logger = LoggerFactory.getLogger(HttpClient.class);
    private org.eclipse.jetty.client.HttpClient client;

    // Timeout of the call to the Ambient Weather devices API
    // FIXME(denisacostaq@gmail.com): more semantic
    public static final int DEVICES_API_TIMEOUT = 20000;

    // Time to wait after failed key validation
    public static final long KEY_VALIDATION_DELAY = 60L;

    public HttpClient(org.eclipse.jetty.client.HttpClient httpClient) {
        this.client = httpClient;
    }

    /**
     * mapping errors as described in https://developer.accuweather.com/apis
     * 
     * @param resp from the accuweather.com server
     * @return a mapping exception if any
     */
    private HttpErrorResponseException mapHttpResponseToException(ContentResponse resp) {
        if (!Objects.equals(Response.Status.Family.SUCCESSFUL,
                Response.Status.fromStatusCode(resp.getStatus()).getFamily())) {
            switch (resp.getStatus()) {
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    return new HttpErrorResponseException(resp.getStatus(),
                            "Request had bad syntax or the parameters supplied were invalid.");
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    return new HttpErrorResponseException(resp.getStatus(), "Unauthorized. API authorization failed.");
                case HttpURLConnection.HTTP_FORBIDDEN:
                    return new HttpErrorResponseException(resp.getStatus(),
                            "Unauthorized. You do not have permission to access this endpoint.");
                case HttpURLConnection.HTTP_NOT_FOUND:
                    return new HttpErrorResponseException(resp.getStatus(),
                            "Server has not found a route matching the given URI.");
                case HttpURLConnection.HTTP_INTERNAL_ERROR:
                    return new HttpErrorResponseException(resp.getStatus(),
                            "Server encountered an unexpected condition which prevented it from fulfilling the request.");
                default:
                    return new HttpErrorResponseException(resp.getStatus(), "Unknown/unexpected error response.");
            }
        }
        return null;
    }

    private String makeGetHttpRequest(String url) throws HttpErrorResponseException, RemoteErrorResponseException {
        try {
            // ContentResponse resp = client.GET(url + "f");
            ContentResponse resp = client.GET(url);
            HttpErrorResponseException ex = mapHttpResponseToException(resp);
            if (!Objects.isNull(ex)) {
                // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Invalid API or
                // application key");
                // // rescheduleValidateKeysJob();
                throw ex;
            }
            return resp.getContentAsString();
        } catch (IllegalArgumentException e) {
            // TODO(denisacostaq@gmail.com): handle this
            logger.warn("} catch (IllegalArgumentException e) {");
            logger.debug("Http client: Got IllegalArgumentException: {}", e.getMessage());
            // FIXME(denisacostaq@gmail.com): setThingOfflineWithCommError(e.getMessage(), "Unable to get devices");
            // rescheduleValidateKeysJob();
        } catch (InterruptedException e) {
            // TODO(denisacostaq@gmail.com): handle this
            logger.warn("} catch (InterruptedException e) {}", e.getMessage());
            logger.debug("Http client: Got InterruptedException: {}", e.getMessage());
        } catch (ExecutionException e) {
            logger.debug("Http client: Got ExecutionException: {}", e.getMessage());
            if (e.getMessage().contains("java.net.ConnectException") && e.getMessage().contains("Connection refused")) {
                throw new RemoteErrorResponseException(RemoteErrorResponseException.StatusType.BAD_SERVER,
                        "Server inaccesible");
            }
        } catch (TimeoutException e) {
            logger.debug("Http client: Got TimeoutException: {}", e.getMessage());
            // TODO(denisacostaq@gmail.com): handle this
            logger.warn("} catch (TimeoutException e) {}", e.getMessage());
        }
        return "";
    }

    @Override
    public String getAdminAreas(String countryDomainName, String apiKey)
            throws HttpErrorResponseException, RemoteErrorResponseException {
        final String ADMIN_AREAS_URL = "http://dataservice.accuweather.com/locations/v1/adminareas/%COUNTRY_DOMAIN_NAME_CODE%?apikey=%API_KEY%";
        String url = ADMIN_AREAS_URL.replace("%COUNTRY_DOMAIN_NAME_CODE%", countryDomainName).replace("%API_KEY%",
                apiKey);
        // FIXME(denisacostaq@gmail.com): Use the build url instead
        url = "http://localhost:8000/Admin_Area_List.json";
        logger.debug("Bridge: Querying Admin (results narrowed by countryCode) from Accuweather service");
        return makeGetHttpRequest(url);
    }

    /**
     *
     * @return the city key
     */
    @Override
    public String citySearch(String countryDomainName, String adminCodeId, String cityNameQuery, String apiKey)
            throws HttpErrorResponseException, RemoteErrorResponseException {
        final String CITY_SEARCH_URL = "http://dataservice.accuweather.com/locations/v1/cities/%COUNTRY_DOMAIN_NAME_CODE%/%ADMIN_CODE%/search?apikey=%API_KEY%&q=%CITY_NAME_QUERY%";
        String url = CITY_SEARCH_URL.replace("%COUNTRY_DOMAIN_NAME_CODE%", countryDomainName)
                .replace("%ADMIN_CODE%", adminCodeId).replace("%API_KEY%", apiKey)
                .replace("%CITY_NAME_QUERY%", cityNameQuery);
        // FIXME(denisacostaq@gmail.com): logger.debug("Validating API key through getting cities API");
        // FIXME(denisacostaq@gmail.com): Use the build url instead
        url = "http://localhost:8000/City_Search_results_narrowed_by_countryCode_and_adminCode_.json";
        logger.debug(
                "Bridge: Querying City Search (results narrowed by countryCode and adminCode) from Accuweather service");
        return makeGetHttpRequest(url);
    }

    @Override
    public String neighborsCities(String cityKey, String apiKey)
            throws HttpErrorResponseException, RemoteErrorResponseException {
        final String NEIGHBORS_CITIES_URL = "http://dataservice.accuweather.com/locations/v1/cities/neighbors/%CITY_KEY%?apikey=%API_KEY%";
        String url = NEIGHBORS_CITIES_URL.replace("%CITY_KEY%", cityKey).replace("%API_KEY%", apiKey);
        // FIXME(denisacostaq@gmail.com): logger.debug("Validating API key through getting cities API");
        // FIXME(denisacostaq@gmail.com): Use the build url instead
        url = "http://localhost:8000/City_NeighborsbylocationKey.json";
        logger.debug("Bridge: Querying Neighbors Cities (results narrowed by city) from Accuweather service");
        return makeGetHttpRequest(url);
    }

    @Override
    public String getCurrentConditions(String cityKey, String apiKey)
            throws HttpErrorResponseException, RemoteErrorResponseException {
        final String CURRENT_CONDITIONS_URL = "http://dataservice.accuweather.com/currentconditions/v1/%CITY_KEY%?apikey=%API_KEY%";
        String url = CURRENT_CONDITIONS_URL.replace("%CITY_KEY%", cityKey).replace("%API_KEY%", apiKey);
        // FIXME(denisacostaq@gmail.com): Use the builded url instead
        url = "http://localhost:8000/Current_Conditions.json";
        logger.debug("Bridge: Getting current conditions");
        return makeGetHttpRequest(url);
    }
}
