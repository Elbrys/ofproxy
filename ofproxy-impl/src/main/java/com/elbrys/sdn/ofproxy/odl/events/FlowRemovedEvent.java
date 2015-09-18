package com.elbrys.sdn.ofproxy.odl.events;

import lombok.Getter;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@lombok.AllArgsConstructor(staticName = "create")
public class FlowRemovedEvent implements ODLEvent {
    @Getter
    SwitchFlowRemoved flow;

    @SuppressWarnings("unchecked")
    @Override
    public InstanceIdentifier<Node> getNodePath() {
        return (InstanceIdentifier<Node>) flow.getNode().getValue();
    }
    
    @Override
    public boolean isCheckNode() {
        return true;
    }
}
