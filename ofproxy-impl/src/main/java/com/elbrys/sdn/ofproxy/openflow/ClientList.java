package com.elbrys.sdn.ofproxy.openflow;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * List of trhid party controllers connected to ODL node
 * 
 * @author igork
 * 
 */
public final class ClientList {

    private final ConcurrentHashMap<String, Client> clients;

    /**
     * ClientList constructor
     */
    public ClientList() {
        clients = new ConcurrentHashMap<String, Client>();
    }

    /**
     * Returns list of connections
     * 
     * @return list of connections
     */
    public final Map<String, Client> getClients() {
        return Collections.unmodifiableMap(clients);
    }

    /**
     * Adds connection to the list
     * 
     * @param client
     *            Client connection
     */
    public final void addConnection(Client client) {
        clients.putIfAbsent(client.getKey(), client);
    }

    /**
     * Removes connection to third party controller
     * 
     * @param client
     *            connection to be removed
     */
    public final void removeConnection(Client client) {
        clients.remove(client.getConfig().getKey());
    }

    /**
     * Closes all connections
     */
    public void stop() {
        for (Client client : clients.values()) {
            client.close();
        }
    }
}
