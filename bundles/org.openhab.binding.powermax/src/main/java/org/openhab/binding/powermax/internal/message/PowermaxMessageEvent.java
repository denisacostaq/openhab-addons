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
package org.openhab.binding.powermax.internal.message;

import java.util.EventObject;

/**
 * Event for messages received from the Visonic alarm panel
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxMessageEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private PowermaxBaseMessage message;

    public PowermaxMessageEvent(Object source, PowermaxBaseMessage message) {
        super(source);
        this.message = message;
    }

    /**
     * @return the message object built from the received message
     */
    public PowermaxBaseMessage getMessage() {
        return message;
    }
}
