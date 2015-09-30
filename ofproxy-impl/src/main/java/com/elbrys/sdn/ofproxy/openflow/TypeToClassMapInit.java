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

package com.elbrys.sdn.ofproxy.openflow;

import java.util.Map;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.TypeToClassKey;
import org.opendaylight.openflowjava.protocol.impl.util.TypeToClassInitHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;

import com.elbrys.sdn.ofproxy.openflow.protocol.OF10FeaturesRequestMessage;

/**
 * Class represents mapping between OF message type and class representing the
 * message
 * 
 * @author Igor Kondrakhin
 * 
 */
public final class TypeToClassMapInit {

    public static void initializeTypeToClassMap(final Map<TypeToClassKey, Class<?>> messageClassMap) {
        // TOD implement commented OF messages
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
        helper.registerTypeToClass((short) 10, PacketInMessage.class);
        helper.registerTypeToClass((short) 11, FlowRemovedMessage.class);
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
