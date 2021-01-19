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

package org.openhab.binding.accuweather.internal.exceptions;

import java.io.IOException;

import javax.ws.rs.core.Response;

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
public class HttpErrorResponseException extends IOException {
    private final Response.StatusType status;
    private final String msg;

    public HttpErrorResponseException(final int statusCode, final String msg) {
        this.status = Response.Status.fromStatusCode(statusCode);
        this.msg = msg;
    }

    public int statusCode() {
        return status.toEnum().getStatusCode();
    }

    @Override
    @Nullable
    public String getMessage() {
        return msg;
    }

    @Override
    public String toString() {
        return "HttpErrorResponseException{" + "status=" + "{family:" + status.getFamily().toString() + ", reason:"
                + status.toEnum().toString() + "}" + ", msg='" + msg + '\'' + '}';
    }
}
