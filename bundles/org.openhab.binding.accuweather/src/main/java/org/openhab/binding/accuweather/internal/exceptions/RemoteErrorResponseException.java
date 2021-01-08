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

package org.openhab.binding.accuweather.internal.exceptions;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.accuweather.internal.handler.AccuweatherHandlerFactory;

/**
 * The {@link AccuweatherHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class RemoteErrorResponseException extends IOException {
    public enum StatusType {
        BAD_SERVER {
            @Override
            public String toString() {
                return "An unexpected error in the remote";
            }
        },
        BAD_CLIENT {
            @Override
            public String toString() {
                return "Client error";
            }
        },
        BAD_CREDENTIALS {
            @Override
            public String toString() {
                return "Invalid credentials";
            }
        },
        BAD_PERMISSIONS {
            @Override
            public String toString() {
                return "Invalid permissions";
            }
        },
        BAD_RESOURCE {
            @Override
            public String toString() {
                return "Can not find resource";
            }
        },
        UNKNOWN {
            @Override
            public String toString() {
                return "Unknown error";
            }
        }
    }

    private final StatusType status;
    private final String msg;

    private StatusType mapHttpErrorCodeToStatusType(final int status) {
        switch (status) {
            case HttpURLConnection.HTTP_BAD_REQUEST:
                return StatusType.BAD_CLIENT;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                return StatusType.BAD_CREDENTIALS;
            case HttpURLConnection.HTTP_FORBIDDEN:
                return StatusType.BAD_PERMISSIONS;
            case HttpURLConnection.HTTP_NOT_FOUND:
                return StatusType.BAD_RESOURCE;
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                return StatusType.BAD_SERVER;
            default:
                return StatusType.UNKNOWN;
        }
    }

    public RemoteErrorResponseException(final HttpErrorResponseException ex) {
        this.status = mapHttpErrorCodeToStatusType(ex.statusCode());
        this.msg = this.status.toString();
    }

    public RemoteErrorResponseException(final StatusType status, final String cause) {
        this.status = status;
        this.msg = cause;
    }

    @Override
    @Nullable
    public String getMessage() {
        return msg;
    }

    public StatusType status() {
        return this.status;
    }
}
