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

import static org.openhab.binding.coap.internal.CoAPBindingConstants.THING_TYPE_URL;

import java.util.Set;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.coap.internal.transform.CascadedValueTransformationImpl;
import org.openhab.binding.coap.internal.transform.NoOpValueTransformation;
import org.openhab.binding.coap.internal.transform.ValueTransformation;
import org.openhab.binding.coap.internal.transform.ValueTransformationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.transform.TransformationHelper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CoAPHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.coap", service = ThingHandlerFactory.class)
public class CoAPHandlerFactory extends BaseThingHandlerFactory
        implements ValueTransformationProvider, CoAPClientProvider {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_URL);
    private final Logger logger = LoggerFactory.getLogger(CoAPHandlerFactory.class);

    private final CoapClient insecureClient;

    private final CoAPDynamicStateDescriptionProvider coapDynamicStateDescriptionProvider;

    @Activate
    public CoAPHandlerFactory(@Reference CoAPDynamicStateDescriptionProvider coapDynamicStateDescriptionProvider) {
        this.insecureClient = new CoapClient();
        try {
            this.insecureClient.useExecutor();
        } catch (Exception e) {
            logger.warn("Failed to start insecure coap client: {}", e.getMessage());
            throw new IllegalStateException("Could not create insecure coapClient");
        }
        this.coapDynamicStateDescriptionProvider = coapDynamicStateDescriptionProvider;
    }

    @Deactivate
    public void deactivate() {
        try {
            insecureClient.shutdown();
        } catch (Exception e) {
            logger.warn("Failed to shutdown coap client: {}", e.getMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_URL.equals(thingTypeUID)) {
            return new CoAPThingHandler(thing, this, this, coapDynamicStateDescriptionProvider);
        }

        return null;
    }

    @Override
    public ValueTransformation getValueTransformation(@Nullable String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return NoOpValueTransformation.getInstance();
        }
        return new CascadedValueTransformationImpl(pattern,
                name -> TransformationHelper.getTransformationService(bundleContext, name));
    }

    @Override
    public CoapClient getInsecureClient() {
        return insecureClient;
    }
}
