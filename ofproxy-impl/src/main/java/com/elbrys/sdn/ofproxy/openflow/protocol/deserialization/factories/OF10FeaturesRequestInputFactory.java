/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.elbrys.sdn.ofproxy.openflow.protocol.deserialization.factories;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;

import com.elbrys.sdn.ofproxy.openflow.protocol.OF10FeaturesRequestMessage;
import com.elbrys.sdn.ofproxy.openflow.protocol.OF10FeaturesRequestMessageBuilder;

public final class OF10FeaturesRequestInputFactory implements OFDeserializer<OF10FeaturesRequestMessage> {

    @Override
    public OF10FeaturesRequestMessage deserialize(final ByteBuf rawMessage) {
        OF10FeaturesRequestMessageBuilder builder = new OF10FeaturesRequestMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        if (rawMessage.readableBytes() > 0) {
            rawMessage.skipBytes(rawMessage.readableBytes());
        }
        return builder.build();
    }

}
