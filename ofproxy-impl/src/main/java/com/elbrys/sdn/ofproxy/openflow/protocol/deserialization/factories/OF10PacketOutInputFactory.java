/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.elbrys.sdn.ofproxy.openflow.protocol.deserialization.factories;

import io.netty.buffer.ByteBuf;

import java.util.List;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMakerFactory;
import org.opendaylight.openflowjava.protocol.impl.util.ListDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInputBuilder;

public final class OF10PacketOutInputFactory implements OFDeserializer<PacketOutInput>, DeserializerRegistryInjector {

    private DeserializerRegistry registry;

    @Override
    public PacketOutInput deserialize(final ByteBuf rawMessage) {
        PacketOutInputBuilder builder = new PacketOutInputBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        builder.setBufferId(rawMessage.readUnsignedInt());
        builder.setInPort(new PortNumber((long) rawMessage.readUnsignedShort()));
        int actionListSize = rawMessage.readShort();
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF10_VERSION_ID);
        List<Action> actions = ListDeserializer.deserializeList(EncodeConstants.OF10_VERSION_ID, actionListSize,
                rawMessage, keyMaker, registry);
        builder.setAction(actions);
        int remainingBytes = rawMessage.readableBytes();
        if (remainingBytes > 0) {
            builder.setData(rawMessage.readBytes(remainingBytes).array());
        }
        return builder.build();
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

}
