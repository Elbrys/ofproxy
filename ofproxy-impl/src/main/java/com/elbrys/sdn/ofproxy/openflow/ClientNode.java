package com.elbrys.sdn.ofproxy.openflow;

import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.Activator;

/**
 * Helper class providing useful ODL data for ODL node
 * 
 * @author igork
 * 
 */
public class ClientNode {
    private static final Logger LOG = LoggerFactory.getLogger(ClientNode.class);

    private final InstanceIdentifier<Node> nodePath;

    public ClientNode(final InstanceIdentifier<Node> nodePath) {
        LOG.debug("ClientNode instantiated. Node path {} ", nodePath);
        this.nodePath = nodePath;
    }

    /**
     * Returns ODL node path
     * @return ODL node path
     */
    public InstanceIdentifier<Node> getNodePath() {
        return nodePath;
    }

    /**
     * Returns ODL node object
     * @return ODL node object
     */
    public Node getNode() {
        return Activator.getConfigObject(nodePath);
    }

    /**
     * Returns ODL FlowCapableNode object
     * @return ODL FlowCapableNode
     */
    public FlowCapableNode getFlowCapableNode() {
        Node node = getNode();
        if (node == null) {
            return null;
        }

        return node.getAugmentation(FlowCapableNode.class);
    }

    /**
     * Returns Datapath ID
     * @return Datapath ID
     */
    public BigInteger getDatapathId() {
        String daylightDpID = getNode().getId().getValue();
        String[] split = daylightDpID.split(":");

        // If the length is just one then this cannot be the new MD-SAL
        // style node connector Id which is of the form openflow:1.
        String dpidStr;
        if (split.length == 1) {
            dpidStr = daylightDpID;
        } else {
            dpidStr = split[split.length - 1];
        }

        return new BigInteger(dpidStr);
    }

    /**
     * Provides port number from ODL NodeConnectorId
     * @param nodeConnectorId ODL NodeConnectorId
     * @return port number
     */
    public static long getPortNfromNodeConnectorId(final NodeConnectorId nodeConnectorId) {
        String ncId = nodeConnectorId.getValue();
        String[] split = ncId.split(":");
        // If the length is just one then this cannot be the new MD-SAL
        // style node connector Id which is of the form openflow:1:3.
        if (split.length == 1) {
            if (ncId.equals("LOCAL")) {
                return 0xfffffffe;
            } else if (ncId.equals("CONTROLLER")) {
                return 0xfffffffd;
            } else if (ncId.equals("NORMAL")) {
                return 0xfffffffa;
            } else {
                return Long.decode(ncId).intValue();
            }
        }
        String portNoString = split[split.length - 1];
        if (portNoString.equals("LOCAL")) {
            return 0xfffffffe;
        } else {
            return Long.decode(portNoString).intValue();
        }
    }

    /**
     * Returns ODL NodeConnectorId by port number
     * @param portN Port number
     * @return ODL NodeConnectorId
     */
    public NodeConnectorId getNodeConnectorIdByPortNumber(final long portN) {
        Node node = getNode();
        if (node == null || node.getId() == null) {
            return null;
        }
        if (portN == 0xfffffffe) {
            // Stolen from package
            // org.opendaylight.controller.sal.compatibility;
            // public static NodeConnectorId toLocalNodeConnectorId(final NodeId
            // node)
            return new NodeConnectorId(node.getId().getValue() + ":" + OutputPortValues.LOCAL.toString());
        } else {
            return new NodeConnectorId(node.getId().getValue() + ":" + String.valueOf(portN & 0xffffffffL));
        }
    }

    /**
     * Return node connector reference by port number.
     * 
     * @param ofPort
     *            Port number
     * @return Node connector reference
     */
    public NodeConnectorRef getNodeConnectorRef(final long ofPort) {
        Node node = getNode();
        if (node == null)
            return null;
        NodeId nodeId = node.getId();
        if (nodeId == null)
            return null;
        StringBuilder sBuild = new StringBuilder(nodeId.getValue()).append(":").append(ofPort & 0xffffffffL);

        return getNodeConnectorRefByNodeId(new NodeConnectorId(sBuild.toString()));
    }

    /**
     * Return node connector reference by node connector Id.
     * 
     * @param ofPort
     *            Port number
     * @return Node connector reference
     */
    public NodeConnectorRef getNodeConnectorRefByNodeId(final NodeConnectorId nodeId) {

        NodeConnectorKey nConKey = new NodeConnectorKey(nodeId);

        InstanceIdentifier<NodeConnector> path = InstanceIdentifier.<Nodes> builder(Nodes.class)
                .<Node, NodeKey> child(Node.class, getNodeKey())
                .<NodeConnector, NodeConnectorKey> child(NodeConnector.class, nConKey).toInstance();
        return new NodeConnectorRef(path);
    }

    /**
     * Return node key.
     * 
     * @return Node key
     */
    public NodeKey getNodeKey() {
        return nodePath.firstKeyOf(Node.class, NodeKey.class);
    }

}
