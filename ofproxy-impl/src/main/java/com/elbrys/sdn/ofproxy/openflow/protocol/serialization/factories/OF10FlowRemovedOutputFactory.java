/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.elbrys.sdn.ofproxy.openflow.protocol.serialization.factories;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.extensibility.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;

public final class OF10FlowRemovedOutputFactory implements OFSerializer<FlowRemovedMessage>, SerializerRegistryInjector {
    private static final byte MESSAGE_TYPE = 11;
    private static final byte PADDING_IN_FLOW_REMOVED_1 = 1;
    private static final byte PADDING_IN_FLOW_REMOVED_2 = 2;

    private SerializerRegistry registry;

    @Override
    public void serialize(final FlowRemovedMessage message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        OFSerializer<MatchV10> matchSerializer = registry.getSerializer(new MessageTypeKey<>(
                EncodeConstants.OF10_VERSION_ID, MatchV10.class));
        matchSerializer.serialize(message.getMatchV10(), outBuffer);
        outBuffer.writeLong(message.getCookie().longValue());
        outBuffer.writeShort(message.getPriority().shortValue());
        outBuffer.writeByte(message.getReason().getIntValue());
        outBuffer.writeZero(PADDING_IN_FLOW_REMOVED_1);
        outBuffer.writeInt(message.getDurationSec().intValue());
        outBuffer.writeInt(message.getDurationNsec().intValue());
        outBuffer.writeShort(message.getIdleTimeout());
        outBuffer.writeZero(PADDING_IN_FLOW_REMOVED_2);
        outBuffer.writeLong(message.getPacketCount().longValue());
        outBuffer.writeLong(message.getByteCount().longValue());
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        this.registry = serializerRegistry;
    }
}
