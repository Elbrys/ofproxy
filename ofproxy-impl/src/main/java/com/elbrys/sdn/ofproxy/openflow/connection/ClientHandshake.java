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

package com.elbrys.sdn.ofproxy.openflow.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.ElementsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.Client;
import com.google.common.collect.Lists;

/**
 * Class is responsible for managing OF handshake
 * @author Igor Kondrakhin
 *
 */
public final class ClientHandshake {
    private static final Logger LOG = LoggerFactory.getLogger(ClientHandshake.class);
    private static ClientHandshake instance = null;
    
    private Client client;
    private boolean helloReceived;
    
    public ClientHandshake(final Client client) {
        instance = this;
        this.client = client;
        helloReceived = false;
    }
    
    public void start() {
        /**
         * supported version ordered by height (highest version is at the
         * beginning)
         */
        // List<Short> versionOrder = Lists.newArrayList((short) 0x04, (short)
        // 0x01);
        try {
            List<Short> versionOrder = Lists.newArrayList((short) 0x01);
            HelloInput helloInput = createHelloInput((short) 0x01, client.getXid(), versionOrder);
            client.send(helloInput);
        } catch (Exception e) {
            LOG.error("Unable to initiate handshake", e);
        }

    }
    
    private HelloInput createHelloInput(final short helloVersion, final long helloXid, final List<Short> versionOrder) {
        HelloInputBuilder helloInputbuilder = new HelloInputBuilder();
        helloInputbuilder.setVersion(helloVersion);
        helloInputbuilder.setXid(helloXid);
        if (versionOrder != null) {
            
            ElementsBuilder elementsBuilder = new ElementsBuilder();
            elementsBuilder.setType(HelloElementType.VERSIONBITMAP);
            int resultVersionListSize = 0;
            if (!versionOrder.isEmpty()) {
                resultVersionListSize = versionOrder.get(0) + 1;
            }
            List<Boolean> booleanList = new ArrayList<>(resultVersionListSize);
            
            int versionOrderIndex = versionOrder.size() - 1;
            
            while (versionOrderIndex >= 0) {
                short version = versionOrder.get(versionOrderIndex);
                if (version == booleanList.size()) {
                    booleanList.add(true);
                    versionOrderIndex--;
                } else {
                    booleanList.add(false);
                }
            }
            
            elementsBuilder.setVersionBitmap(booleanList);

            List<Elements> elementList = Collections.singletonList(elementsBuilder.build());
            helloInputbuilder.setElements(elementList);
        }
        return helloInputbuilder.build();
    }

    public static ClientHandshake getInstance() {
        return instance;
    }
    
    public boolean isHelloReceived() {
        return helloReceived;
    }
    
    public void onHelloReceived(final HelloMessage hm) {
        LOG.debug("Received Hello Message");
        helloReceived = true;
    }

}
