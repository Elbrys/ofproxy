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

package com.elbrys.sdn.ofproxy.openflow.protocol.serialization;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.openflowjava.protocol.api.extensibility.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.impl.deserialization.TypeToClassKey;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.TypeToClassMapInit;

/**
 * OF message encoder
 * 
 * @author igork
 * 
 */
public final class MsgEncoder {
    private static final Logger LOG = LoggerFactory.getLogger(MsgEncoder.class);

    private SerializerRegistry registry;
    private SerializationFactory factory;
    private Map<TypeToClassKey, Class<?>> messageClassMap;

    public MsgEncoder() {
        registry = new OFSerializerRegistryImpl();
        registry.init();
        factory = new SerializationFactory();
        factory.setSerializerTable(registry);
        messageClassMap = new HashMap<TypeToClassKey, Class<?>>();
        TypeToClassMapInit.initializeTypeToClassMap(messageClassMap);
    }

    /**
     * Transforms POJO message into ByteBuf
     * 
     * @param version
     *            version used for encoding received message
     * @param out
     *            ByteBuf for storing and sending transformed message
     * @param message
     *            POJO message
     */
    public void messageToBuffer(final short version, final ByteBuf out, final DataObject message) {
        OFSerializer<DataObject> serializer = registry.getSerializer(new MessageTypeKey<>(version, message
                .getImplementedInterface()));
        if (serializer != null) {
            try {
                serializer.serialize(message, out);
            } catch (Exception e) {
                LOG.warn("Unable to serialize message {}", message, e);
            }
        } else {
            LOG.debug("Unable to find serialiszer for {}", message);
        }
    }

}
