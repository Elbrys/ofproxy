package com.elbrys.sdn.ofproxy.openflow;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

/**
 * Class represents message to OF controller
 * @author igork
 *
 */
@lombok.AllArgsConstructor(staticName = "create")
@ToString
public final class ClientMsg {
    @Getter
    Client client;
    @Getter
    ByteBuf msg;

}
