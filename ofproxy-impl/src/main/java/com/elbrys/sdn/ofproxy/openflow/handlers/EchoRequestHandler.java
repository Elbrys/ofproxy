package com.elbrys.sdn.ofproxy.openflow.handlers;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.Client;

public final class EchoRequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(EchoRequestHandler.class);

    public static void consume(final OFClientMsg msg) {
        Client client = msg.getClient();
        EchoReplyInput erm = getEchoReply((EchoRequestMessage)msg.getMsg()); 
        if (erm == null) {
            LOG.warn("Unable to create echo reply.");
            return;
        }
        client.send(erm);
        LOG.trace("EchoReply message is sent to the switch.");
    }

    private static EchoReplyInput getEchoReply(final EchoRequestMessage msg) {
        EchoReplyInputBuilder eib = new EchoReplyInputBuilder();
        eib.setVersion(msg.getVersion());
        eib.setXid(msg.getXid());
        eib.setData(msg.getData());
        return eib.build();
    }

}
