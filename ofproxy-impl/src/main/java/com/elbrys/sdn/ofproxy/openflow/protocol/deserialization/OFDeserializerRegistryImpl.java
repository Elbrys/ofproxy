package com.elbrys.sdn.ofproxy.openflow.protocol.deserialization;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFGeneralDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.ActionDeserializerInitializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.MatchEntryDeserializerInitializer;
import org.opendaylight.openflowjava.protocol.impl.util.OF10MatchDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OFDeserializerRegistryImpl implements DeserializerRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(OFDeserializerRegistryImpl.class);

    private Map<MessageCodeKey, OFGeneralDeserializer> registry;

    @Override
    public void init() {
        registry = new HashMap<>();
        // register message deserializers
        OFMessageDeserialezerInit.registerMessageDeserializers(this);
        // register common structure deserializers
        registerDeserializer(new MessageCodeKey(EncodeConstants.OF10_VERSION_ID,
                EncodeConstants.EMPTY_VALUE, MatchV10.class), new OF10MatchDeserializer());
        // register match entry deserializers
        MatchEntryDeserializerInitializer.registerMatchEntryDeserializers(this);
        // register action deserializers
        ActionDeserializerInitializer.registerDeserializers(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <DESERIALIZER_TYPE extends OFGeneralDeserializer> DESERIALIZER_TYPE getDeserializer(final MessageCodeKey key) {
        OFGeneralDeserializer deserializer = registry.get(key);
        if (deserializer == null) {
            throw new IllegalStateException("Deserializer for key: " + key
                    + " was not found - please verify that all needed deserializers ale loaded correctly");
        }
        return (DESERIALIZER_TYPE) deserializer;
    }

    @Override
    public void registerDeserializer(final MessageCodeKey key, final OFGeneralDeserializer deserializer) {
        if ((key == null) || (deserializer == null)) {
            throw new IllegalArgumentException("MessageCodeKey or Deserializer is null");
        }
        OFGeneralDeserializer desInRegistry = registry.put(key, deserializer);
        if (desInRegistry != null) {
            LOG.debug("Deserializer for key " + key + " overwritten. Old deserializer: "
                    + desInRegistry.getClass().getName() + ", new deserializer: " + deserializer.getClass().getName());
        }
        if (deserializer instanceof DeserializerRegistryInjector) {
            ((DeserializerRegistryInjector) deserializer).injectDeserializerRegistry(this);
        }
    }

    @Override
    public boolean unregisterDeserializer(final MessageCodeKey key) {
        if (key == null) {
            throw new IllegalArgumentException("MessageCodeKey is null");
        }
        OFGeneralDeserializer deserializer = registry.remove(key);
        if (deserializer == null) {
            return false;
        }
        return true;
    }

}
