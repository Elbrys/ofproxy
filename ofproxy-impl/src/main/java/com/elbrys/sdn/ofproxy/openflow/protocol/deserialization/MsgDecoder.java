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


public final class MsgDecoder{
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
//        ByteBuf bb = (ByteBuf) rawMessage;
//        if (LOG.isDebugEnabled()) {
////            LOG.debug("<< " + ByteBufUtils.byteBufToHexString(bb));
//        }

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
