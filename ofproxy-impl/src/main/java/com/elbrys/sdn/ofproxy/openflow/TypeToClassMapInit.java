/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.elbrys.sdn.ofproxy.openflow;

import java.util.Map;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.TypeToClassKey;
import org.opendaylight.openflowjava.protocol.impl.util.TypeToClassInitHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;

import com.elbrys.sdn.ofproxy.openflow.protocol.OF10FeaturesRequestMessage;

public final class TypeToClassMapInit {

    public static void initializeTypeToClassMap(final Map<TypeToClassKey, Class<?>> messageClassMap) {
        // init OF v1.0 mapping
        TypeToClassInitHelper helper = 
                new TypeToClassInitHelper(EncodeConstants.OF10_VERSION_ID, messageClassMap);
        helper.registerTypeToClass((short) 0, HelloMessage.class);
//        helper.registerTypeToClass((short) 1, ErrorMessage.class);
        helper.registerTypeToClass((short) 2, EchoRequestMessage.class);
//        helper.registerTypeToClass((short) 3, EchoOutput.class);
//        helper.registerTypeToClass((short) 4, ExperimenterMessage.class);
        helper.registerTypeToClass((short) 5, OF10FeaturesRequestMessage.class);
//        helper.registerTypeToClass((short) 6, GetFeaturesOutput.class);
//        helper.registerTypeToClass((short) 8, GetConfigOutput.class);
        helper.registerTypeToClass((short) 9, SetConfigInput.class);
//        helper.registerTypeToClass((short) 10, PacketInMessage.class);
//        helper.registerTypeToClass((short) 11, FlowRemovedMessage.class);
//        helper.registerTypeToClass((short) 12, PortStatusMessage.class);
        helper.registerTypeToClass((short) 13, PacketOutInput.class);
        helper.registerTypeToClass((short) 14, FlowModInput.class);
        helper.registerTypeToClass((short) 16, MultipartRequestInput.class);
        helper.registerTypeToClass((short) 17, MultipartReplyMessage.class);
        helper.registerTypeToClass((short) 18, BarrierInput.class);
        helper.registerTypeToClass((short) 19, BarrierOutput.class);
//        helper.registerTypeToClass((short) 21, GetQueueConfigOutput.class);
//        // init OF v1.0 mapping
//        helper = new TypeToClassInitHelper(EncodeConstants.OF13_VERSION_ID, messageClassMap);
//        helper.registerTypeToClass((short) 0, HelloMessage.class);
//        helper.registerTypeToClass((short) 1, ErrorMessage.class);
//        helper.registerTypeToClass((short) 2, EchoRequestMessage.class);
//        helper.registerTypeToClass((short) 3, EchoOutput.class);
//        helper.registerTypeToClass((short) 4, ExperimenterMessage.class);
//        helper.registerTypeToClass((short) 6, GetFeaturesOutput.class);
//        helper.registerTypeToClass((short) 8, GetConfigOutput.class);
//        helper.registerTypeToClass((short) 10, PacketInMessage.class);
//        helper.registerTypeToClass((short) 11, FlowRemovedMessage.class);
//        helper.registerTypeToClass((short) 12, PortStatusMessage.class);
//        helper.registerTypeToClass((short) 19, MultipartReplyMessage.class);
//        helper.registerTypeToClass((short) 21, BarrierOutput.class);
//        helper.registerTypeToClass((short) 23, GetQueueConfigOutput.class);
//        helper.registerTypeToClass((short) 25, RoleRequestOutput.class);
//        helper.registerTypeToClass((short) 27, GetAsyncOutput.class);
    }
}
