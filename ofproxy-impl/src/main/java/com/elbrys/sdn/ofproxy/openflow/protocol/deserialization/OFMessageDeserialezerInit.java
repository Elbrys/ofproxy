package com.elbrys.sdn.ofproxy.openflow.protocol.deserialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.EchoRequestMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.OF10HelloMessageFactory;
import org.opendaylight.openflowjava.protocol.impl.util.SimpleDeserializerRegistryHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;

import com.elbrys.sdn.ofproxy.openflow.protocol.OF10FeaturesRequestMessage;
import com.elbrys.sdn.ofproxy.openflow.protocol.deserialization.factories.OF10BarrierRequestInputFactory;
import com.elbrys.sdn.ofproxy.openflow.protocol.deserialization.factories.OF10FeaturesRequestInputFactory;
import com.elbrys.sdn.ofproxy.openflow.protocol.deserialization.factories.OF10FlowModInputFactory;
import com.elbrys.sdn.ofproxy.openflow.protocol.deserialization.factories.OF10PacketOutInputFactory;
import com.elbrys.sdn.ofproxy.openflow.protocol.deserialization.factories.OF10SetConfigInputFactory;
import com.elbrys.sdn.ofproxy.openflow.protocol.deserialization.factories.OF10StatsRequestInputFactory;

public final class OFMessageDeserialezerInit {
    /**
     * Registers message deserializers
     * @param registry registry to be filled with deserializers
     */
    public static void registerMessageDeserializers(final DeserializerRegistry registry) {
        // register OF v1.0 message deserializers
        SimpleDeserializerRegistryHelper helper =
                new SimpleDeserializerRegistryHelper(EncodeConstants.OF10_VERSION_ID, registry);
        helper.registerDeserializer(0, null, HelloMessage.class, new OF10HelloMessageFactory());
        helper.registerDeserializer(2, null, EchoRequestMessage.class, new EchoRequestMessageFactory());
        helper.registerDeserializer(5, null, OF10FeaturesRequestMessage.class, new OF10FeaturesRequestInputFactory());
        helper.registerDeserializer(9, null, SetConfigInput.class, new OF10SetConfigInputFactory());
        helper.registerDeserializer(13, null, PacketOutInput.class, new OF10PacketOutInputFactory());
        helper.registerDeserializer(14, null, FlowModInput.class, new OF10FlowModInputFactory());
        helper.registerDeserializer(16, null, MultipartRequestInput.class, new OF10StatsRequestInputFactory());
        helper.registerDeserializer(18, null, BarrierInput.class, new OF10BarrierRequestInputFactory());
//        helper.registerDeserializer(16, null, MultipartRequestInput.class, new OF10StatsRequestInputDeserializerFactory());
//        helper.registerDeserializer(10, null, PacketInMessage.class, new OF10PacketInMessageFactory());
////        helper.registerDeserializer(15, null, FlowModInput.class, new OF10FlowModInputFactory());
//        helper.registerDeserializer(0, null, HelloMessage.class, new OF10HelloMessageFactory());
//        helper.registerDeserializer(1, null, ErrorMessage.class, new OF10ErrorMessageFactory());
//        helper.registerDeserializer(2, null, EchoRequestMessage.class, new OF10EchoRequestMessageFactory());
//        helper.registerDeserializer(3, null, EchoOutput.class, new OF10EchoReplyMessageFactory());
//        helper.registerDeserializer(6, null, GetFeaturesOutput.class, new OF10FeaturesReplyMessageFactory());
//        helper.registerDeserializer(8, null, GetConfigOutput.class, new OF10GetConfigReplyMessageFactory());
//        helper.registerDeserializer(10, null, PacketInMessage.class, new OF10PacketInMessageFactory());
//        helper.registerDeserializer(11, null, FlowRemovedMessage.class, new OF10FlowRemovedMessageFactory());
//        helper.registerDeserializer(12, null, PortStatusMessage.class, new OF10PortStatusMessageFactory());
//        helper.registerDeserializer(17, null, MultipartReplyMessage.class, new OF10StatsReplyMessageFactory());
//        helper.registerDeserializer(19, null, BarrierOutput.class, new OF10BarrierReplyMessageFactory());
//        helper.registerDeserializer(21, null, GetQueueConfigOutput.class, new OF10QueueGetConfigReplyMessageFactory());
//        // register Of v1.3 message deserializers
//        helper = new SimpleDeserializerRegistryHelper(EncodeConstants.OF13_VERSION_ID, registry);
//        helper.registerDeserializer(0, null, HelloMessage.class, new HelloMessageFactory());
//        helper.registerDeserializer(1, null, ErrorMessage.class, new ErrorMessageFactory());
//        helper.registerDeserializer(2, null, EchoRequestMessage.class, new EchoRequestMessageFactory());
//        helper.registerDeserializer(3, null, EchoOutput.class, new EchoReplyMessageFactory());
//        helper.registerDeserializer(6, null, GetFeaturesOutput.class, new FeaturesReplyMessageFactory());
//        helper.registerDeserializer(8, null, GetConfigOutput.class, new GetConfigReplyMessageFactory());
//        helper.registerDeserializer(10, null, PacketInMessage.class, new PacketInMessageFactory());
//        helper.registerDeserializer(11, null, FlowRemovedMessage.class, new FlowRemovedMessageFactory());
//        helper.registerDeserializer(12, null, PortStatusMessage.class, new PortStatusMessageFactory());
//        helper.registerDeserializer(19, null, MultipartReplyMessage.class, new MultipartReplyMessageFactory());
//        helper.registerDeserializer(21, null, BarrierOutput.class, new BarrierReplyMessageFactory());
//        helper.registerDeserializer(23, null, GetQueueConfigOutput.class, new QueueGetConfigReplyMessageFactory());
//        helper.registerDeserializer(25, null, RoleRequestOutput.class, new RoleReplyMessageFactory());
//        helper.registerDeserializer(27, null, GetAsyncOutput.class, new GetAsyncReplyMessageFactory());
    }
}
