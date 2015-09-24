package com.elbrys.sdn.ofproxy.odl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeExperimenterErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SwitchFlowRemoved;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.impl.OpendaylightMgr;
import com.elbrys.sdn.ofproxy.odl.events.FlowRemovedEvent;

/**
 * ODL FLow event listener Current version supports only forwarding of
 * FlowRemoved messages.
 * 
 * @author igork
 * 
 */
public class FlowListener implements SalFlowListener {
    private static final Logger LOG = LoggerFactory.getLogger(FlowListener.class);

    private final OpendaylightMgr odlMgr;

    public FlowListener(final OpendaylightMgr opendaylightMgr) {
        this.odlMgr = opendaylightMgr;
    }

    @Override
    public void onFlowAdded(final FlowAdded flow) {
        // LOG.error("=====  onFlowAdded  {}", flow);
    }

    @Override
    public void onFlowRemoved(final FlowRemoved flow) {
        // LOG.error("=====  onFlowRemoved  {}", flow);
    }

    @Override
    public void onFlowUpdated(final FlowUpdated flow) {
        // LOG.error("=====  onFlowUpdated  {}", flow);
    }

    @Override
    public void onNodeErrorNotification(final NodeErrorNotification error) {
        // LOG.error("=====  onNodeErrorNotification  {}", error);
    }

    @Override
    public void onNodeExperimenterErrorNotification(final NodeExperimenterErrorNotification arg0) {
        // LOG.error("=====  onNodeExperimenterErrorNotification  {}", arg0);
    }

    @Override
    public final void onSwitchFlowRemoved(final SwitchFlowRemoved flowRemoved) {
        LOG.debug("Flow removed {}", flowRemoved);
        odlMgr.odlEvent(FlowRemovedEvent.create(flowRemoved));
    }

}
