/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.coap.internal.config;

import java.util.ArrayList;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link CoAPThingConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class CoAPThingConfig {

    public String baseURL = "";
    public int refresh = 30;
    public int timeout = 3000;
    public int delay = 0;

    public String username = "";
    public String password = "";

    public CoAPAuthMode authMode = CoAPAuthMode.BASIC;

    public Code stateMethod = CoAP.Code.GET;

    // "CoAP supports the basic methods of GET, POST, PUT, DELETE, which are easily mapped to HTTP."
    public Code commandMethod = CoAP.Code.GET;

    public int bufferSize = 255; // by default

    public @Nullable String encoding = null;
    public @Nullable String contentType = null;

    public ArrayList<String> headers = new ArrayList<>();
}
