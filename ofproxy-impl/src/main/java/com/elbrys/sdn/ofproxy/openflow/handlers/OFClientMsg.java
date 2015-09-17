package com.elbrys.sdn.ofproxy.openflow.handlers;

import lombok.Getter;
import lombok.ToString;

import org.opendaylight.yangtools.yang.binding.DataObject;

import com.elbrys.sdn.ofproxy.openflow.Client;

@lombok.AllArgsConstructor(staticName = "create")
@ToString
public final class OFClientMsg {
    @Getter
    Client client;
    @Getter
    DataObject msg;

}
