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

package org.openhab.binding.accuweather.internal.util;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link Util} have some helper function.
 *
 * @author Alvaro Denis <denisacostaq@gmail.com> - Initial contribution
 */
public class Util {

    /**
     * make sure that there are not null arguments
     * 
     * @param vals mapping from argument name to argument value
     * @param methodName function name
     */
    public static void ensureNotNull(Map<String, Object> vals, String methodName) {
        vals.keySet().stream().forEach(k -> {
            Objects.requireNonNull(vals.get(k), String.format("%s should not be null in %s", k, methodName));
            if (vals.get(k) instanceof String) {
                if (StringUtils.isEmpty((String) vals.get(k))) {
                    throw new IllegalArgumentException(String.format("%s should not be empty in %s", k, methodName));
                }
            }
        });
    }
}
