package com.elbrys.sdn.ofproxy.openflow.handlers;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.connection.ClientHandshake;
import com.elbrys.sdn.ofproxy.openflow.protocol.OF10FeaturesRequestMessage;

public final class OFMsgHandler {
    private static final Logger LOG = LoggerFactory.getLogger(OFMsgHandler.class);
    
    OF10StatsRequestHandler oF10StatRequestHandler;
    OF10FlowModInputHandler oF10FlowModHandler;
    
    public OFMsgHandler(final ConsumerContext sess) {
        oF10StatRequestHandler = new OF10StatsRequestHandler(sess);
        oF10FlowModHandler = new OF10FlowModInputHandler();
    }
    
    public void consume(final OFClientMsg msg) {
            try {
                DataObject om = msg.getMsg();
                if (om instanceof HelloMessage) {
                    LOG.debug(" Received Hello");
                    ClientHandshake.getInstance().onHelloReceived((HelloMessage) om);
                } else if (om instanceof OF10FeaturesRequestMessage) {
                    LOG.debug(" Received FeaturesRequestMessage.");
                    OF10FeaturesRequestHandler.consume(msg);
                } else if (om instanceof SetConfigInput) {
                    LOG.debug(" Received SetConfigInput.");
                    SetConfigInputHandler.consume(msg);
                } else if (om instanceof MultipartRequestInput) {
//                    LOG.debug(" Received MultipartRequestInput.");
//                    oF10StatRequestHandler.consume(msg);
                } else if (om instanceof PacketOutInput) {
//                    LOG.trace(" Received PacketOutInput.");
                    OF10PacketOutInputHandler.consume(msg);
                } else if (om instanceof FlowModInput) {
                    LOG.trace(" Received FlowModInput.");
                    oF10FlowModHandler.consume(msg);
                } else if (om instanceof EchoRequestMessage) {
                    LOG.trace(" Received EchoRequestMessage.");
                    EchoRequestHandler.consume(msg);
                } else if (om instanceof BarrierInput) {
                    LOG.trace(" Received BarrierInput.");
                    // TODO ODL allows to send Barrier Request to particular node only
                    // as part of FlowMod message.
                    LOG.trace(" ODL does not allow to send separate Barrier Mesage.");
                } else {
                    LOG.debug(" >>>>>>>>>>>  Unexpected message {}.", om);
                }

            } catch (Exception e) {
                LOG.debug("parseOFMsg ", e);
            }
    }
}
