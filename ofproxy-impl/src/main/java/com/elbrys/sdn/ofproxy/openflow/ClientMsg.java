package com.elbrys.sdn.ofproxy.openflow;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@lombok.AllArgsConstructor(staticName = "create")
@ToString
public final class ClientMsg {
    @Getter
    Client client;
    @Getter
    ByteBuf msg;

}
