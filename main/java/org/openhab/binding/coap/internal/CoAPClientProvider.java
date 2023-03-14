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
package org.openhab.binding.coap.internal;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CoAPClientProvider} defines the interface for providing {@link CoapClient} instances to thing handlers
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public interface CoAPClientProvider {

    /**
     * get the insecure coap client (ignores DTLS errors)
     * Let it be only insecure client in our project
     *
     * @return a CoapClient
     */
    CoapClient getInsecureClient();
}
