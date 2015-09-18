/*
 * Copyright (c) 2012, Elbrys Networks
 * All Rights Reserved.
 */

package com.elbrys.sdn.ofproxy.odl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;

import com.elbrys.sdn.ofproxy.impl.OpendaylightMgr;
import com.elbrys.sdn.ofproxy.odl.events.PacketInEvent;

public final class PacketInListener implements PacketProcessingListener {

    private final OpendaylightMgr odlMgr;

    public PacketInListener(final OpendaylightMgr opendaylightMgr) {
        this.odlMgr = opendaylightMgr;
    }

    @Override
    public void onPacketReceived(final PacketReceived pkt) {
        odlMgr.odlEvent(PacketInEvent.create(pkt));
    }
}
