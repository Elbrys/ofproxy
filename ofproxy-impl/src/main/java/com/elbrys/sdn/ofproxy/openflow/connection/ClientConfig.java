package com.elbrys.sdn.ofproxy.openflow.connection;

import lombok.Getter;
import lombok.ToString;

@lombok.AllArgsConstructor(staticName = "create")
@ToString
public final class ClientConfig {
    @Getter
    String host;
    @Getter
    int port;
    @Getter
    boolean secured;

    public final String getKey() {
        return host+String.valueOf(port);
    }
}
