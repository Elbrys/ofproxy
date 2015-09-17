package com.elbrys.sdn.ofproxy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.impl.OpendaylightMgr;
import com.elbrys.sdn.ofproxy.impl.OpenflowMgr;
import com.elbrys.sdn.ofproxy.openflow.ClientList;
import com.elbrys.sdn.ofproxy.openflow.Client;
import com.elbrys.sdn.ofproxy.openflow.connection.ClientConfig;

public final class OFProxy {
    private static final Logger LOG = LoggerFactory.getLogger(OFProxy.class);
    private static OFProxy instance = null;
    
    private ExecutorService executor;
    private OpenflowMgr ofMgr;
    private OpendaylightMgr odlMgr;
    private ConcurrentHashMap<InstanceIdentifier<Node>, ClientList> clients;

	public OFProxy(final ConsumerContext sess) {
        LOG.debug("OFProxy constructor started.");
		executor = Executors.newCachedThreadPool();
		ofMgr = new OpenflowMgr(sess, executor);
        odlMgr = new OpendaylightMgr(sess, executor);
        LOG.debug("OFProxy constructor finished.");
        clients = new ConcurrentHashMap<InstanceIdentifier<Node>, ClientList>();
        OFProxy.instance = this;
	}

	public static OFProxy getInstance() {
	    return instance;
	}
    
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

	public void start() {
        LOG.debug("OFProxy starting openflow manager.");
        ofMgr.start();
        LOG.debug("OFProxy starting opendaylight manager.");
        odlMgr.start();
        LOG.debug("OFProxy started.");
	}

    public void addODLNode(InstanceIdentifier<Node> nodePath, ClientConfig cfg) {
        // try to establish connection
        Client client = ofMgr.addClient(nodePath, cfg);
        if (client != null) {
            // Register client
            ClientList cl  = clients.get(nodePath);
            if (cl == null) {
                cl = new ClientList();
                clients.putIfAbsent(nodePath, cl);
            }
            cl.addConnection(client);
        }
    }

    public ClientList getClientList(InstanceIdentifier<Node> nodePath) {
        return clients.get(nodePath);
    }

    public void removeODLNode(InstanceIdentifier<Node> node) {
        ClientList cl = clients.remove(node);
        if (cl != null) {
            cl.stop();
        }
    }
}
