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

import java.util.Map.Entry;

import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.impl.OpendaylightMgr;
import com.elbrys.sdn.ofproxy.odl.events.AddNodeEvent;
import com.elbrys.sdn.ofproxy.odl.events.RemoveNodeEvent;

/**
 * ODL node event listener
 * 
 * @author igork
 * 
 */
public final class NodeListener implements DataChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(NodeListener.class);
    private final OpendaylightMgr odlMgr;

    /**
     * NodeListener constructor
     * @param opendaylightMgr
     */
    public NodeListener(OpendaylightMgr opendaylightMgr) {
        this.odlMgr = opendaylightMgr;
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> event) {
        // Check removed nodes
        for (final InstanceIdentifier<?> removedItem : event.getRemovedPaths()) {
            InstanceIdentifier<Node> nodePath = removedItem.firstIdentifierOf(Node.class);
            if (nodePath == null) {
                LOG.error("Unable to find node info. {}", removedItem);
                continue;
            }
            // Daylight report a lot of stuff removed with FlowCapableNode,
            // such as removed tables, flows, etc. So, skip everything related
            // to the FlowCapable node and only report removed FlowCapableNode
            if (removedItem.getTargetType() == FlowCapableNode.class) {
                nodeRemoved(nodePath);
            }
        }

        // Check added nodes
        for (Entry<InstanceIdentifier<?>, DataObject> addedItem : event.getCreatedData().entrySet()) {
            if (addedItem.getValue() instanceof FlowCapableNode) {
                // Switch capabilities can be taken from FlowCapableNode if
                // necessary
                InstanceIdentifier<Node> nodePath = addedItem.getKey().firstIdentifierOf(Node.class);
                LOG.debug("Node {}", (FlowCapableNode) addedItem.getValue());
                if (nodePath == null) {
                    LOG.error("Unable to find node info. {}", addedItem.getKey());
                    continue;
                }
                nodeAdded(nodePath);
            }
            if (addedItem.getValue() instanceof FlowCapableNodeConnector) {
                LOG.debug("Nodeconnector {}", (FlowCapableNodeConnector) addedItem.getValue());
            }
        }

        // Current implementation do not provide data for updated nodes.
    }

    private void nodeAdded(final InstanceIdentifier<Node> nodePath) {
        LOG.debug("Node added {}", nodePath);
        odlMgr.odlEvent(AddNodeEvent.create(nodePath));
    }

    private void nodeRemoved(final InstanceIdentifier<Node> nodePath) {
        LOG.debug("Node removed {}", nodePath);
        odlMgr.odlEvent(RemoveNodeEvent.create(nodePath));
    }
}
