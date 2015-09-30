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
 * @author Igor Kondrakhin
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
