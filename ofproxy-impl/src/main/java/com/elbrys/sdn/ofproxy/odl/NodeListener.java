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

public final class NodeListener implements DataChangeListener {
    private static final Logger LOG = LoggerFactory
            .getLogger(NodeListener.class);
    private final OpendaylightMgr odlMgr;

    public NodeListener(OpendaylightMgr opendaylightMgr) {
        this.odlMgr = opendaylightMgr;
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> event) {
        // Check removed nodes
        for (final InstanceIdentifier<?> removedItem : event
                .getRemovedPaths()) {
            InstanceIdentifier<Node> nodePath = removedItem
                    .firstIdentifierOf(Node.class);
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
        for (Entry<InstanceIdentifier<?>, DataObject> addedItem : event
                .getCreatedData().entrySet()) {
            if (addedItem.getValue() instanceof FlowCapableNode) {
                // Switch capabilities can be taken from FlowCapableNode if
                // necessary
                InstanceIdentifier<Node> nodePath =
                    addedItem.getKey().firstIdentifierOf(Node.class);
                LOG.debug("Node {}", (FlowCapableNode)addedItem.getValue());
                if (nodePath == null) {
                    LOG.error("Unable to find node info. {}",
                            addedItem.getKey());
                    continue;
                }
                nodeAdded(nodePath);
            }
            if (addedItem.getValue() instanceof FlowCapableNodeConnector) {
                LOG.debug("Nodeconnector {}", (FlowCapableNodeConnector)addedItem.getValue());
            }
        }

        // Current implementation do not provide data for updated nodes.
    }

    private void nodeRemoved(final InstanceIdentifier<Node> nodePath) {
        LOG.debug("Node removed {}", nodePath);
        odlMgr.odlEvent(RemoveNodeEvent.create(nodePath));
    }


    private void nodeAdded(final InstanceIdentifier<Node> nodePath) {
        LOG.debug("Node added {} OFProxy {}", nodePath, odlMgr);
        odlMgr.odlEvent(AddNodeEvent.create(nodePath));
    }
}
