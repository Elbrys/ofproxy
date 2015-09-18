package com.elbrys.sdn.ofproxy.odl.events;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface ODLEvent {

    InstanceIdentifier<Node> getNodePath();
    
    boolean isCheckNode();
}
