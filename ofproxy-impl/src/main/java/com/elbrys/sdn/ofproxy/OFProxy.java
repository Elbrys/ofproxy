package com.elbrys.sdn.ofproxy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.impl.OpendaylightMgr;
import com.elbrys.sdn.ofproxy.impl.OpenflowMgr;
import com.elbrys.sdn.ofproxy.openflow.ClientList;
import com.elbrys.sdn.ofproxy.openflow.Client;
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
    private ConcurrentHashMap<InstanceIdentifier<Node>, ClientList> clients;
    private ConcurrentHashMap<String, ClientConfig> clientsCfg;

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
        clients = new ConcurrentHashMap<InstanceIdentifier<Node>, ClientList>();
        clientsCfg = new ConcurrentHashMap<String, ClientConfig>();
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
     * Creates connection to third party controller.
     * 
     * @param nodePath
     *            ODL node
     * @param cfg
     *            Controller connection configuration
     */
    public void addConnection(InstanceIdentifier<Node> nodePath, ClientConfig cfg) {
        // try to establish connection
        Client client = ofMgr.addConnection(nodePath, cfg);
        if (client != null) {
            // Register client
            ClientList cl = clients.get(nodePath);
            if (cl == null) {
                cl = new ClientList();
                clients.putIfAbsent(nodePath, cl);
            }
            cl.addConnection(client);
        }
    }

    /**
     * Removes connection to third party controllers connected to target node
     * 
     * @param node
     *            ODL node
     */
    public void removeConnections(InstanceIdentifier<Node> node) {
        ClientList cl = clients.remove(node);
        if (cl != null) {
            cl.stop();
        }
    }

    /**
     * Returns list of third party controllers connected to target node
     * 
     * @param nodePath
     *            ODL node
     * @return list of third party controllers connected to target node
     */
    public ClientList getConnections(InstanceIdentifier<Node> nodePath) {
        if (nodePath == null) {
            return null;
        }
        return clients.get(nodePath);
    }

    /**
     * Adds client configuration
     * @param datapathId ODL node datapathId
     * @param controllerIp Third-party controller IP
     * @param controllerPort Third party controller port
     */
    public String addConnection(String datapathId, String controllerIp, Integer controllerPort) {
        if (clientsCfg.put(datapathId, ClientConfig.create(controllerIp, controllerPort, false)) != null) {
            return "New client configuration has been added.";
        } else {
            return "Unable to add client configuration.";
        }
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
}
