package com.elbrys.sdn.ofproxy.odl.events.handlers;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.odl.events.AddNodeEvent;
import com.elbrys.sdn.ofproxy.odl.events.FlowRemovedEvent;
import com.elbrys.sdn.ofproxy.odl.events.ODLEvent;
import com.elbrys.sdn.ofproxy.odl.events.RemoveNodeEvent;

public final class ODLEventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ODLEventHandler.class);

    public ODLEventHandler(final ConsumerContext sess) {
    }

    public void consume(final ODLEvent msg) {
        try {
            if (msg instanceof AddNodeEvent) {
                AddNodeHandler.consume((AddNodeEvent) msg);
            } else if (msg instanceof RemoveNodeEvent) {
                RemoveNodeHandler.consume((RemoveNodeEvent) msg);
            } else if (msg instanceof FlowRemovedEvent) {
                FlowRemovedHandler.consume((FlowRemovedEvent) msg);
            } else {
                LOG.debug(" >>>>>>>>>>>  Unexpected ODL event {}.", msg);
            }

        } catch (Exception e) {
            LOG.debug("parseOFMsg ", e);
        }
    }
}
