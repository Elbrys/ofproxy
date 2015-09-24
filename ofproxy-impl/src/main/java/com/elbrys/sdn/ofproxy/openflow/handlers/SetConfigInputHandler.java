package com.elbrys.sdn.ofproxy.openflow.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SetConfigInputHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SetConfigInputHandler.class);

    public static void consume(final OFClientMsg msg) {
        LOG.debug("Do something on setConfig message {}", msg.getMsg());
    }
}
