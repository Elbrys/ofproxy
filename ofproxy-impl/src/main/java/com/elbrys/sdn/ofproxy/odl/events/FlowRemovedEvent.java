package com.elbrys.sdn.ofproxy.odl.events;

import lombok.Getter;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SwitchFlowRemoved;

@lombok.AllArgsConstructor(staticName = "create")
public class FlowRemovedEvent implements ODLEvent {
    @Getter
    SwitchFlowRemoved flow;

}
