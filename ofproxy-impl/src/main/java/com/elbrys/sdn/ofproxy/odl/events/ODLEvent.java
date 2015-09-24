package com.elbrys.sdn.ofproxy.odl.events;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * ODL events interface
 * 
 * @author igork
 * 
 */
public interface ODLEvent {

    /**
     * Return ODL node responsible for event
     * 
     * @return ODL node
     */
    InstanceIdentifier<Node> getNodePath();

    /**
     * Check if node should be verified before consuming event
     * 
     * @return false if event should be consumed even if node is unknown to
     *         OFProxy yet, true - check if node is already managed by OFProxy
     */
    boolean isCheckNode();
}
