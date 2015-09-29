/*
 * Copyright (c) 2015,  BROCADE COMMUNICATIONS SYSTEMS, INC
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.elbrys.sdn.ofproxy.openflow.protocol.deserialization;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.TypeToClassKey;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.TypeToClassMapInit;

/**
 * OF message decoder
 * 
 * @author igork
 * 
 */
public final class MsgDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(MsgDecoder.class);

    private DeserializerRegistry registry;
    private DeserializationFactory factory;
    private Map<TypeToClassKey, Class<?>> messageClassMap;

    public MsgDecoder() {
        registry = new OFDeserializerRegistryImpl();
        registry.init();
        factory = new DeserializationFactory();
        factory.setRegistry(registry);
        messageClassMap = new HashMap<TypeToClassKey, Class<?>>();
        TypeToClassMapInit.initializeTypeToClassMap(messageClassMap);
    }

    public DataObject deserialize(final ByteBuf rawMessage) {
        // ByteBuf bb = (ByteBuf) rawMessage;
        // if (LOG.isDebugEnabled()) {
        // // LOG.debug("<< " + ByteBufUtils.byteBufToHexString(bb));
        // }

        DataObject dataObject = null;
        int type = -1;
        try {
            // short version = EncodeConstants.OF10_VERSION_ID;
            short version = rawMessage.readUnsignedByte();
            type = rawMessage.readUnsignedByte();
            Class<?> clazz = messageClassMap.get(new TypeToClassKey(version, type));
            int msgLength = rawMessage.readUnsignedShort();
            if (version != EncodeConstants.OF10_VERSION_ID) {
                LOG.debug("Unsupported version: {}. Skip OF message");
                rawMessage.skipBytes(msgLength - 4);
                return dataObject;
            }
            OFDeserializer<DataObject> deserializer = registry
                    .getDeserializer(new MessageCodeKey(version, type, clazz));
            if (deserializer != null) {
                dataObject = deserializer.deserialize(rawMessage);
            } else {
                LOG.warn("Unable to deserialize type {}  {}", type, ByteBufUtils.byteBufToHexString(rawMessage));
            }
        } catch (Exception e) {
            LOG.debug("Unable to deserialize message.type {} ", type, e);
        }
        rawMessage.release();
        return dataObject;
    }

}
