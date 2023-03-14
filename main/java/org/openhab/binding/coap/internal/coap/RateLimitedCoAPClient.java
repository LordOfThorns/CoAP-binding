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

import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RateLimitedCoAPClient} is a wrapper for a Californium CoAP client that limits the number of requests by
 * delaying
 * the request creation
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class RateLimitedCoAPClient {
    private static final int MAX_QUEUE_SIZE = 1000; // maximum queue size
    private CoapClient coapClient;
    private int delay = 0; // in ms
    private final ScheduledExecutorService scheduler;
    private final LinkedBlockingQueue<RequestQueueEntry> requestQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

    private @Nullable ScheduledFuture<?> processJob;

    public RateLimitedCoAPClient(CoapClient coapClient, ScheduledExecutorService scheduler) {
        this.coapClient = coapClient;
        this.scheduler = scheduler;
    }

    /**
     * Stop processing the queue and clear it
     */
    public void shutdown() {
        stopProcessJob();
        requestQueue.forEach(queueEntry -> queueEntry.future.completeExceptionally(new CancellationException()));
    }

    /**
     * Set a new delay
     *
     * @param delay in ms between to requests
     */
    public void setDelay(int delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Delay needs to be larger or equal to zero");
        }
        this.delay = delay;
        stopProcessJob();
        if (delay != 0) {
            processJob = scheduler.scheduleWithFixedDelay(this::processQueue, 0, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Set the CoAP client
     *
     * @param coapClient Californium coap client
     */
    public void setCoapClient(CoapClient coapClient) {
        this.coapClient = coapClient;
    }

    /**
     * Create a new request to the given URL respecting rate-limits
     *
     * @param finalUrl the request URL
     * @param method CoAP request method GET/PUT/POST
     * @param content the content (if method PUT/POST)
     * @return a CompletableFuture that completes with the request
     */
    public CompletableFuture<Request> newRequest(URI finalUri, CoAP.Code method, String content) {
        // if no delay is set, return a completed CompletableFuture
        CompletableFuture<Request> future = new CompletableFuture<>();
        RequestQueueEntry queueEntry = new RequestQueueEntry(finalUri, method, content, future);
        if (delay == 0) {
            queueEntry.completeFuture(coapClient);
        } else {
            if (!requestQueue.offer(queueEntry)) {
                future.completeExceptionally(new RejectedExecutionException("Maximum queue size exceeded."));
            }
        }
        return future;
    }

    // /**
    // * Get the AuthenticationStore from the wrapped client
    // *
    // * @return
    // */
    // public AuthenticationStore getAuthenticationStore() {
    // return coapClient.getAuthenticationStore();
    // }

    private void stopProcessJob() {
        ScheduledFuture<?> processJob = this.processJob;
        if (processJob != null) {
            processJob.cancel(false);
            this.processJob = null;
        }
    }

    @SuppressWarnings("null")
    private void processQueue() {
        RequestQueueEntry queueEntry = requestQueue.poll();
        if (queueEntry != null) {
            queueEntry.completeFuture(coapClient);
        }
    }

    private static class RequestQueueEntry {
        private URI finalUri;
        private CoAP.Code method;
        private String content;
        private CompletableFuture<Request> future;

        public RequestQueueEntry(URI finalUrl2, Code method2, String content, CompletableFuture<Request> future) {
            this.finalUri = finalUrl2;
            this.method = method2;
            this.content = content;
            this.future = future;
        }

        /**
         * complete the future with a request
         *
         * @param coapClient the client to create the request
         */
        public void completeFuture(CoapClient coapClient) {

            Request request = new Request(method);

            future.complete(request);
        }
    }
}
