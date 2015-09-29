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

import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.EchoReplyInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10FlowModInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10HelloInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10PacketOutInputMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.CommonMessageRegistryHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;

import com.elbrys.sdn.ofproxy.openflow.protocol.serialization.factories.OF10FeatureReplyOutputFactory;
import com.elbrys.sdn.ofproxy.openflow.protocol.serialization.factories.OF10FlowRemovedOutputFactory;
import com.elbrys.sdn.ofproxy.openflow.protocol.serialization.factories.OF10PacketInMessageOutputFactory;
import com.elbrys.sdn.ofproxy.openflow.protocol.serialization.factories.OF10StatsReplyOutputFactory;

public final class OFMessageSerializerInit {

    /**
     * Registers message serializers into provided registry
     * @param serializerRegistry registry to be initialized with message serializers
     */
    public static void registerMessageSerializers(final SerializerRegistry serializerRegistry) {
        // register OF v1.0 message serializers
        short version = EncodeConstants.OF10_VERSION_ID;
        CommonMessageRegistryHelper registryHelper = new CommonMessageRegistryHelper(version, serializerRegistry);
        registryHelper.registerSerializer(HelloInput.class, new OF10HelloInputMessageFactory());
        registryHelper.registerSerializer(EchoReplyInput.class, new EchoReplyInputMessageFactory());
        registryHelper.registerSerializer(GetFeaturesOutput.class, new OF10FeatureReplyOutputFactory());
        registryHelper.registerSerializer(PacketOutInput.class, new OF10PacketOutInputMessageFactory());
        registryHelper.registerSerializer(PacketInMessage.class, new OF10PacketInMessageOutputFactory());
        registryHelper.registerSerializer(FlowRemoved.class, new OF10FlowRemovedOutputFactory());
        registryHelper.registerSerializer(FlowModInput.class, new OF10FlowModInputMessageFactory());
        registryHelper.registerSerializer(MultipartReplyMessage.class, new OF10StatsReplyOutputFactory());
 //        registryHelper.registerSerializer(BarrierInput.class, new OF10BarrierInputMessageFactory());
//        registryHelper.registerSerializer(EchoReplyInput.class, new EchoReplyInputMessageFactory());
//        registryHelper.registerSerializer(FlowModInput.class, new OF10FlowModInputMessageFactory());
//        registryHelper.registerSerializer(GetConfigInput.class, new GetConfigInputMessageFactory());
//        registryHelper.registerSerializer(GetFeaturesInput.class, new GetFeaturesInputMessageFactory());
//        registryHelper.registerSerializer(GetQueueConfigInput.class, new OF10QueueGetConfigInputMessageFactory());
//        registryHelper.registerSerializer(MultipartRequestInput.class, new OF10StatsRequestInputFactory());
//        registryHelper.registerSerializer(PortModInput.class, new OF10PortModInputMessageFactory());
//        registryHelper.registerSerializer(SetConfigInput.class, new SetConfigMessageFactory());
    }
}
