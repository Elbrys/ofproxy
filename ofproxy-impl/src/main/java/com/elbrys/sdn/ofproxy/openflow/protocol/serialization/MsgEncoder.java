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


public final class MsgEncoder{
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
     * @param version version used for encoding received message
     * @param out ByteBuf for storing and sending transformed message
     * @param message POJO message
     */
    public void messageToBuffer(final short version, final ByteBuf out, final DataObject message) {
        OFSerializer<DataObject> serializer = registry.getSerializer(
                new MessageTypeKey<>(version, message.getImplementedInterface()));
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
