package com.elbrys.sdn.ofproxy.openflow;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.elbrys.sdn.ofproxy.openflow.connection.ClientConfig;

/**
 * List of configured controllers
 * 
 * @author igork
 * 
 */
public final class ClientConfigList {

    private final ConcurrentHashMap<String, ClientConfig> clients;

    /**
     * ClientConfigList constructor
     */
    public ClientConfigList() {
        clients = new ConcurrentHashMap<String, ClientConfig>();
    }

    /**
     * Returns list of configured controllers
     * 
     * @return list of configured controllers
     */
    public final Map<String, ClientConfig> getClients() {
        return Collections.unmodifiableMap(clients);
    }

    /**
     * Adds client to the list
     * 
     * @param client
     *            Client config
     */
    public final void addClient(ClientConfig client) {
        clients.putIfAbsent(client.getKey(), client);
    }

    /**
     * Removes third party controller from configuration
     * 
     * @param client
     *            config to be removed
     */
    public final void removeClient(ClientConfig client) {
        clients.remove(client.getKey());
    }

}
