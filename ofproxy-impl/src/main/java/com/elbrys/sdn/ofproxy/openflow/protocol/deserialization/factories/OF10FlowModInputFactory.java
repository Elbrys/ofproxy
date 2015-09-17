/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.elbrys.sdn.ofproxy.openflow.protocol.deserialization.factories;

import io.netty.buffer.ByteBuf;

import java.math.BigInteger;
import java.util.List;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMakerFactory;
import org.opendaylight.openflowjava.protocol.impl.util.ListDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlagsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;

public final class OF10FlowModInputFactory implements OFDeserializer<FlowModInput>, DeserializerRegistryInjector {
    private DeserializerRegistry registry;

    @Override
    public FlowModInput deserialize(final ByteBuf rawMessage) {
        FlowModInputBuilder builder = new FlowModInputBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        OFDeserializer<MatchV10> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, EncodeConstants.EMPTY_VALUE, MatchV10.class));
        builder.setMatchV10(matchDeserializer.deserialize(rawMessage));
        builder.setCookie(BigInteger.valueOf(rawMessage.readLong()));
        builder.setCommand(FlowModCommand.forValue(rawMessage.readUnsignedShort()));
        builder.setIdleTimeout(rawMessage.readUnsignedShort());
        builder.setHardTimeout(rawMessage.readUnsignedShort());
        builder.setPriority(rawMessage.readUnsignedShort());
        builder.setBufferId(rawMessage.readUnsignedInt());
        builder.setOutPort(new PortNumber((long) rawMessage.readUnsignedShort()));
        builder.setFlagsV10(createFlowModFlagsV10FromBitmap(rawMessage.readUnsignedShort()));
        int actionListSize = rawMessage.readShort();
        CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF10_VERSION_ID);
        List<Action> actions = ListDeserializer.deserializeList(EncodeConstants.OF10_VERSION_ID,
                actionListSize, rawMessage, keyMaker, registry);
        builder.setAction(actions);
        return builder.build();
    }
    
    private static FlowModFlagsV10 createFlowModFlagsV10FromBitmap(final int input){
        final Boolean sendFlowRem = (input & (1 << 0)) != 0;
        final Boolean checkOverlap = (input & (1 << 1)) != 0;
        final Boolean emergency = (input & (1 << 2)) != 0; 
        // ODL FlowModFlagsV10 constructor uses flag order different from OF.
        return new FlowModFlagsV10(checkOverlap, emergency, sendFlowRem);
    }
    
    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

}
