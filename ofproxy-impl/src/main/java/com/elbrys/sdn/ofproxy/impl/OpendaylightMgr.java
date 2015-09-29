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

package com.elbrys.sdn.ofproxy.impl;

import java.util.concurrent.ExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.OFProxy;
import com.elbrys.sdn.ofproxy.odl.FlowListener;
import com.elbrys.sdn.ofproxy.odl.NodeListener;
import com.elbrys.sdn.ofproxy.odl.PacketInListener;
import com.elbrys.sdn.ofproxy.odl.events.ODLEvent;
import com.elbrys.sdn.ofproxy.odl.queue.ODLEventQueue;

/**
 * Class responsible for managing ODL events.
 * 
 * @author igork
 * 
 */
public final class OpendaylightMgr {
    private static final Logger LOG = LoggerFactory.getLogger(OpendaylightMgr.class);
    private static final int MSG_QUEUE_SIZE = 1000;

    private final ExecutorService executor;
    private final ODLEventQueue odlEvents;
    private final NodeListener nodeMgr;
    private ListenerRegistration<DataChangeListener> nodeLsnrRegistration;
    private final PacketInListener pktMgr;
    private org.opendaylight.yangtools.concepts.Registration packetInRegistration;
    private final FlowListener flowMgr;
    private org.opendaylight.yangtools.concepts.Registration flowRegistration;

    /**
     * OpendaylightMgr constructor
     * 
     * @param sess
     *            ODL consumer context session
     * @param executor
     *            Executor service
     */
    public OpendaylightMgr(final ConsumerContext sess, ExecutorService executor) {
        LOG.debug("Opendaylight manager constructor started.");
        this.executor = executor;
        odlEvents = new ODLEventQueue(MSG_QUEUE_SIZE, sess);
        LOG.debug("Opendaylight manager constructor finished.");
        nodeMgr = new NodeListener(this);
        LOG.debug("Opendaylight manager constructor registering node listener.");
        nodeLsnrRegistration = sess.getSALService(DataBroker.class).registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.builder(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class)
                        .toInstance(), nodeMgr, DataBroker.DataChangeScope.SUBTREE);
        pktMgr = new PacketInListener(this);
        packetInRegistration = sess.getSALService(NotificationService.class).registerNotificationListener(pktMgr);
        flowMgr = new FlowListener(this);
        flowRegistration = sess.getSALService(NotificationService.class).registerNotificationListener(flowMgr);

    }

    /**
     * Stops OpendaylightMgr
     */
    public void stop() {
        if (nodeLsnrRegistration != null) {
            nodeLsnrRegistration.close();
            LOG.debug("unregister nodeListenerRegistration");
        }
        if (packetInRegistration != null) {
            try {
                packetInRegistration.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOG.debug("unregister packetInRegistration");
        }
        if (flowRegistration != null) {
            try {
                flowRegistration.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOG.debug("unregister flowRegistration");
        }
    }

    /**
     * Starts OpendaylightMgr
     */
    public void start() {
        LOG.debug("Opendaylight manager starting odlEvents.");
        executor.execute(odlEvents);
        LOG.debug("Opendaylight manager started.");
    }

    /**
     * Consumes ODL event
     * 
     * @param odlEvent
     *            ODL event
     */
    public void odlEvent(final ODLEvent odlEvent) {

        if (odlEvent.isCheckNode() && null == OFProxy.getInstance().getConnections(odlEvent.getNodePath())) {
            // Skip event if node does not have any thrid party controllers
            // connected
            return;
        }

        if (!odlEvents.offer(odlEvent)) {
            LOG.warn("Unable to queue ODL event. Event {}", odlEvent);
        }
    }
}
