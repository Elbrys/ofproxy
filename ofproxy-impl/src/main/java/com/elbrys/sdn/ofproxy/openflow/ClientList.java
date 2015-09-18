package com.elbrys.sdn.ofproxy.openflow;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class ClientList{
    
    private final ConcurrentHashMap<String, Client> clients;
    
	public ClientList() {
	    clients = new ConcurrentHashMap<String, Client>();
	}
	
	public final Map<String, Client> getClients() {
	    return Collections.unmodifiableMap(clients);
	}
	
    public final void addConnection(Client client) {
        clients.putIfAbsent(client.getKey(), client);
    }
    
    public final void removeConnection(Client client) {
        clients.remove(client.getConfig().getKey());
    }
    
    public final Client getClient(InstanceIdentifier<Node> nodePath) {
        return clients.get(nodePath);
    }

    public void stop() {
        for (Client client:clients.values()) {
            client.close();
        }
    }
}
