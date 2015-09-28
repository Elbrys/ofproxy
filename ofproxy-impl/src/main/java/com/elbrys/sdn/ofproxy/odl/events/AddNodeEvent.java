package com.elbrys.sdn.ofproxy.odl.events;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * ODL AddNode event
 * 
 * @author igork
 * 
 */
@lombok.AllArgsConstructor(staticName = "create")
public class AddNodeEvent implements ODLEvent {
    private InstanceIdentifier<Node> node;

    @Override
    public InstanceIdentifier<Node> getNodePath() {
        return node;
    }

    @Override
    public boolean isCheckNode() {
        return false;
    }
}
