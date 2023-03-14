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
package org.openhab.binding.coap.internal.converter;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.coap.internal.coap.Content;
import org.openhab.binding.coap.internal.config.CoAPChannelConfig;
import org.openhab.binding.coap.internal.config.CoAPChannelMode;
import org.openhab.binding.coap.internal.transform.ValueTransformation;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link AbstractTransformingItemConverter} is a base class for an item converter with transformations
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractTransformingItemConverter implements ItemValueConverter {
    private final Consumer<State> updateState;
    private final Consumer<Command> postCommand;
    private final @Nullable Consumer<String> sendCoapValue;
    private final ValueTransformation stateTransformations;
    private final ValueTransformation commandTransformations;

    protected CoAPChannelConfig channelConfig;

    public AbstractTransformingItemConverter(Consumer<State> updateState, Consumer<Command> postCommand,
            @Nullable Consumer<String> sendCoapValue, ValueTransformation stateTransformations,
            ValueTransformation commandTransformations, CoAPChannelConfig channelConfig) {
        this.updateState = updateState;
        this.postCommand = postCommand;
        this.sendCoapValue = sendCoapValue;
        this.stateTransformations = stateTransformations;
        this.commandTransformations = commandTransformations;
        this.channelConfig = channelConfig;
    }

    @Override
    public void process(Content content) {
        if (channelConfig.mode != CoAPChannelMode.WRITEONLY) {
            stateTransformations.apply(content.getAsString()).ifPresent(transformedValue -> {
                Command command = toCommand(transformedValue);
                if (command != null) {
                    postCommand.accept(command);
                } else {
                    updateState.accept(toState(transformedValue));
                }
            });
        } else {
            throw new IllegalStateException("Write-only channel");
        }
    }

    @Override
    public void send(Command command) {
        Consumer<String> sendCoapValue = this.sendCoapValue;
        if (sendCoapValue != null && channelConfig.mode != CoAPChannelMode.READONLY) {
            commandTransformations.apply(toString(command)).ifPresent(sendCoapValue);
        } else {
            throw new IllegalStateException("Read-only channel");
        }
    }

    /**
     * check if this converter received a value that needs to be sent as command
     *
     * @param value the value
     * @return the command or null
     */
    protected abstract @Nullable Command toCommand(String value);

    /**
     * convert the received value to a state
     *
     * @param value the value
     * @return the state that represents the value of UNDEF if conversion failed
     */
    protected abstract State toState(String value);

    /**
     * convert a command to a string
     *
     * @param command the command
     * @return the string representation of the command
     */
    protected abstract String toString(Command command);

    @FunctionalInterface
    public interface Factory {
        ItemValueConverter create(Consumer<State> updateState, Consumer<Command> postCommand,
                @Nullable Consumer<String> sendCoapValue, ValueTransformation stateTransformations,
                ValueTransformation commandTransformations, CoAPChannelConfig channelConfig);
    }
}
