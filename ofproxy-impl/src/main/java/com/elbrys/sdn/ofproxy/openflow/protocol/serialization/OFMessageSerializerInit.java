/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
