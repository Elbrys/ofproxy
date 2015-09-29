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

package com.elbrys.sdn.ofproxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.impl.OpendaylightMgr;
import com.elbrys.sdn.ofproxy.impl.OpenflowMgr;
import com.elbrys.sdn.ofproxy.openflow.ClientConfigList;
import com.elbrys.sdn.ofproxy.openflow.ClientList;
import com.elbrys.sdn.ofproxy.openflow.connection.ClientConfig;
import com.google.common.base.Optional;

/**
 * Class responsible for activation of two main application parts: Opendaylight
 * and OpenFlow managers.
 * 
 * @author igork
 */
public final class OFProxy {
    private static final Logger LOG = LoggerFactory.getLogger(OFProxy.class);
    private static OFProxy instance = null;
    /** ConsumerContext session. */
    private static ConsumerContext session = null;

    private ExecutorService executor;
    private OpenflowMgr ofMgr;
    private OpendaylightMgr odlMgr;
    private ConcurrentHashMap<String, ClientConfigList> clientsCfg;

    /**
     * OFProxy constructor
     * 
     * @param sess
     *            - ODL consumer context
     */
    public OFProxy(final ConsumerContext sess) {
        session = sess;
        LOG.debug("OFProxy constructor started.");
        executor = Executors.newCachedThreadPool();
        ofMgr = new OpenflowMgr(sess, executor);
        odlMgr = new OpendaylightMgr(sess, executor);
        LOG.debug("OFProxy constructor finished.");
        clientsCfg = new ConcurrentHashMap<String, ClientConfigList>();
        OFProxy.instance = this;
    }

    /**
     * Returns OFProxy instance
     * 
     * @return OFProxy instance
     */
    public static OFProxy getInstance() {
        return instance;
    }

    /**
     * Stops OFProxy
     */
    public void stop() {
        ofMgr.stop();
        odlMgr.stop();
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start OFProxy
     */
    public void start() {
        LOG.debug("OFProxy starting openflow manager.");
        ofMgr.start();
        LOG.debug("OFProxy starting opendaylight manager.");
        odlMgr.start();
        LOG.debug("OFProxy started.");
    }


    /**
     * Adds client configuration
     * @param datapathId ODL node datapathId
     * @param controllerIp Third-party controller IP
     * @param controllerPort Third party controller port
     */
    public String addConnection(String datapathId, String controllerIp, Integer controllerPort) {
        ClientConfigList ccl = clientsCfg.get(datapathId);
        if (ccl == null) {
            ccl = new ClientConfigList();
            clientsCfg.putIfAbsent(datapathId, ccl);
        }
        
        ccl.addClient(ClientConfig.create(controllerIp, controllerPort, false));
        
        checkConnectedNodes();

        // TODO Modify to return some useful data
        return "New client configuration has been added.";
    }

    private void checkConnectedNodes() {
        List<Node> nodes = getConnectedNodes();
        LOG.debug("Check connected nodes");
        for (Node node: nodes) {
            String dpId = getDpId(node);
            LOG.debug("  Node dpid: {}", dpId);
            ClientConfigList ccl = clientsCfg.get(dpId);
            if (ccl == null) {
                LOG.debug(" node not configured to create proxy connections");
                continue;
            }
            
            final InstanceIdentifier<Node> nodePath = InstanceIdentifier.builder(Nodes.class)
                    .child(Node.class, new NodeKey(node.getId())).build();
            
            // Go through list of configured controllers and create connection 
            // if connection is not established.
            for (ClientConfig cc: ccl.getClients().values()) {
                if (!ofMgr.isClientConnected(nodePath, cc)) {
                    // Found not connected client. Initiate connection
                    ofMgr.addConnection(nodePath, cc);
                }
            }
        }
    }

    private String getDpId(Node node) {
        String daylightDpID = node.getId().getValue();
        String[] split = daylightDpID.split(":");

        // If the length is just one then this cannot be the new MD-SAL
        // style node connector Id which is of the form openflow:1.
        String dpidStr;
        if (split.length == 1) {
            dpidStr = daylightDpID;
        } else {
            dpidStr = split[split.length - 1];
        }

        return dpidStr;
    }

    /**
     * Returns object from ODL operational data store.
     * 
     * @param objRef Object reference
     * @return Requested object
     */
    public static <K extends DataObject> K getConfigObject(final InstanceIdentifier<K> objRef) {
        if (session == null) {
            LOG.error("Unable to retrieve config object {}. No session context.", objRef);
            return null;
        }
        ReadOnlyTransaction readTx = session.getSALService(DataBroker.class).newReadOnlyTransaction();
        Optional<K> data;
        try {
            data = readTx.read(LogicalDatastoreType.OPERATIONAL, objRef).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }

        if (!data.isPresent()) {
            return null;
        }

        return data.get();
    }
    
    /**
     * Return list of connected instances.
     * @return Instance list
     */
    public List<Node> getConnectedNodes() {
        List<Node> retVal = new ArrayList<Node>();

        Nodes nodes = getConfigObject(InstanceIdentifier.builder(
                Nodes.class).toInstance());
        if (nodes != null) {
            for (Node node : nodes.getNode()) {
                FlowCapableNode fcn = node
                        .getAugmentation(FlowCapableNode.class);
                if (fcn != null) {
                    retVal.add(node);
                }
            }
        }
        return retVal;
    }

    /**
     * Return list of proxies for target DPID
     * @param dpId target datapathId
     * @return List of configured proxy connections
     */
    public ClientConfigList getConfiguredClient(String dpId) {
        return clientsCfg.get(dpId);
    }

    /**
     * Returns list of third party controllers connected to target node
     * 
     * @param nodePath
     *            ODL node
     * @return list of third party controllers connected to target node
     */
    public ClientList getConnections(InstanceIdentifier<Node> nodePath) {
        return ofMgr.getConnections(nodePath);
    }

    /**
     * Check if connection is established
     * @param nodePath ODL node path
     * @param cc Client configuration
     * @return true if connection is established
     */
    public boolean isClientConnected(InstanceIdentifier<Node> nodePath, ClientConfig cc) {
        return ofMgr.isClientConnected(nodePath, cc);
    }

    /**
     * Creates connection to third party controller.
     * 
     * @param nodePath
     *            ODL node
     * @param cfg
     *            Controller connection configuration
     */
    public void addConnection(InstanceIdentifier<Node> nodePath, ClientConfig cc) {
        ofMgr.addConnection(nodePath, cc);
    }

    /**
     * Removes connection to third party controllers connected to target node
     * 
     * @param node
     *            ODL node
     */
    public void removeConnections(InstanceIdentifier<Node> node) {
        ofMgr.removeConnections(node);
    }

}
