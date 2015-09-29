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

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.openflowjava.protocol.api.extensibility.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.ActionsInitializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.MatchEntriesInitializer;
import org.opendaylight.openflowjava.protocol.impl.util.OF10MatchSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OFSerializerRegistryImpl implements SerializerRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(OFSerializerRegistryImpl.class);

    private Map<MessageTypeKey<?>, OFGeneralSerializer> registry;

    @Override
    public void init() {
        registry = new HashMap<>();
        // Openflow message type serializers
        OFMessageSerializerInit.registerMessageSerializers(this);
        // match structure serializers
        registerSerializer(new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, MatchV10.class),
                new OF10MatchSerializer());
        // match entry serializers
        MatchEntriesInitializer.registerMatchEntrySerializers(this);
        // action serializers
        ActionsInitializer.registerActionSerializers(this);
    }

    /**
     * @param msgTypeKey
     * @return encoder for current type of message (msgTypeKey)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <KEY_TYPE, SERIALIZER_TYPE extends OFGeneralSerializer> SERIALIZER_TYPE getSerializer(
            final MessageTypeKey<KEY_TYPE> msgTypeKey) {
        OFGeneralSerializer serializer = registry.get(msgTypeKey);
        if (serializer == null) {
            throw new IllegalStateException("Serializer for key: " + msgTypeKey
                    + " was not found - please verify that you are using correct message"
                    + " combination (e.g. OF v1.0 message to OF v1.0 device)");
        }
        return (SERIALIZER_TYPE) serializer;
    }

    @Override
    public <KEY_TYPE> void registerSerializer(final MessageTypeKey<KEY_TYPE> msgTypeKey, OFGeneralSerializer serializer) {
        if ((msgTypeKey == null) || (serializer == null)) {
            throw new IllegalArgumentException("MessageTypeKey or Serializer is null");
        }
        OFGeneralSerializer serInRegistry = registry.put(msgTypeKey, serializer);
        if (serInRegistry != null) {
            LOGGER.debug("Serializer for key " + msgTypeKey + " overwritten. Old serializer: "
                    + serInRegistry.getClass().getName() + ", new serializer: " + serializer.getClass().getName());
        }
        if (serializer instanceof SerializerRegistryInjector) {
            ((SerializerRegistryInjector) serializer).injectSerializerRegistry(this);
        }
    }

    @Override
    public <KEY_TYPE> boolean unregisterSerializer(final MessageTypeKey<KEY_TYPE> msgTypeKey) {
        if (msgTypeKey == null) {
            throw new IllegalArgumentException("MessageTypeKey is null");
        }
        OFGeneralSerializer serializer = registry.remove(msgTypeKey);
        if (serializer == null) {
            return false;
        }
        return true;
    }
}
