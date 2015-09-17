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

import com.elbrys.sdn.ofproxy.odl.FlowListener;
import com.elbrys.sdn.ofproxy.odl.NodeListener;
import com.elbrys.sdn.ofproxy.odl.PacketInListener;
import com.elbrys.sdn.ofproxy.odl.events.ODLEvent;
import com.elbrys.sdn.ofproxy.odl.queue.ODLEventQueue;

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

	public OpendaylightMgr(final ConsumerContext sess, ExecutorService executor) {
        LOG.debug("Opendaylight manager constructor started.");
		this.executor = executor;
		odlEvents = new ODLEventQueue(MSG_QUEUE_SIZE, sess);
        LOG.debug("Opendaylight manager constructor finished.");
        nodeMgr = new NodeListener(this);
        LOG.debug("Opendaylight manager constructor registering node listener.");
        nodeLsnrRegistration = sess.getSALService(DataBroker.class).registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.builder(Nodes.class).child(Node.class)
                        .augmentation(FlowCapableNode.class).toInstance(),
                nodeMgr, DataBroker.DataChangeScope.SUBTREE);
        pktMgr = new PacketInListener();
        packetInRegistration = sess.getSALService(NotificationService.class)
                .registerNotificationListener(pktMgr);
        flowMgr = new FlowListener(this);
        flowRegistration = sess.getSALService(NotificationService.class)
                .registerNotificationListener(flowMgr);
        
	}
    
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

	public void start() {
        LOG.debug("Opendaylight manager starting odlEvents.");
	    executor.execute(odlEvents);
        LOG.debug("Opendaylight manager started.");
		
	}

    public void odlEvent(final ODLEvent odlEvent) {
        if (!odlEvents.offer(odlEvent)) {
            LOG.warn("Unable to queue ODL event. Event {}", odlEvent);
        }
    }

}
