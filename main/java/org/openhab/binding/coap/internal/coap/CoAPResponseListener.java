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
package org.openhab.binding.coap.internal.coap;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CoAPResponseListener} is responsible for processing the result of a CoAP request
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class CoAPResponseListener extends BufferingResponseListener {
    private final Logger logger = LoggerFactory.getLogger(CoAPResponseListener.class);
    private final CompletableFuture<@Nullable Content> future;
    private final String fallbackEncoding;

    /**
     * the CoAPResponseListener is responsible
     *
     * @param future Content future to complete with the result of the request
     * @param fallbackEncoding a fallback encoding for the content (UTF-8 if null)
     * @param bufferSize the buffer size for the content in kB (default 255 kB)
     */
    public CoAPResponseListener(CompletableFuture<@Nullable Content> future, @Nullable String fallbackEncoding,
            int bufferSize) {
        super(bufferSize * 1024);
        this.future = future;
        this.fallbackEncoding = fallbackEncoding != null ? fallbackEncoding : StandardCharsets.UTF_8.name();
    }

    @Override
    public void onComplete(@NonNullByDefault({}) Result result) {
        CoapResponse response = (CoapResponse) result.getResponse();
        if (logger.isTraceEnabled()) {
            logger.trace("Received from '{}': {}", result.getRequest().getURI(), responseToLogString(response));
        }
        Request request = (Request) result.getRequest();
        if (result.isFailed()) {
            logger.warn("Requesting '{}' failed: {}", request.getURI(), result.getFailure().toString());
            future.complete(null);
        } else if (response.isSuccess()) {
            String encoding = Objects.requireNonNullElse(getEncoding(), fallbackEncoding);
            future.complete(new Content(getContent(), encoding, getMediaType()));
        } else {
            logger.warn("Requesting '{}' Response code {}", request.getURI(), response.getCode());
            future.completeExceptionally(
                    new IllegalStateException("Response is not successful. Response code " + response.getCode()));
        }
    }

    private String responseToLogString(CoapResponse response) {
        String logString = "Code = {" + response.getCode() + "}, Payload = {" + response.getPayload() + "}, Content = {"
                + response.getResponseText() + "}";
        return logString;
    }
}
