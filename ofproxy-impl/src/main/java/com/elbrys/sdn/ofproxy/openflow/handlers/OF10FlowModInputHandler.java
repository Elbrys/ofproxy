package com.elbrys.sdn.ofproxy.openflow.handlers;

import java.util.concurrent.atomic.AtomicInteger;

import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.OFToMDSalFlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.openflow.Client;

public final class OF10FlowModInputHandler {
    private static final Logger LOG = LoggerFactory.getLogger(OF10FlowModInputHandler.class);
    AtomicInteger flowIdN;

    public OF10FlowModInputHandler() {
        flowIdN = new AtomicInteger(1);
    }

    public void consume(final OFClientMsg msg) {
        // TODO Check if client is allowed to set such flows
        sendFlowMod(msg.getClient(), (FlowModInput) msg.getMsg());
        LOG.trace("FlowMiod message is forwarded to the switch.");
    }

    private void sendFlowMod(final Client client, final FlowModInput msg) {
        FlowId flowId = new FlowId(String.valueOf(flowIdN.incrementAndGet()));
        FlowKey flowKey = new FlowKey(flowId);
        short tableId = 0;
        if (msg.getTableId() != null && msg.getTableId().getValue() != null) {
            tableId = msg.getTableId().getValue().shortValue();
        }
        FlowBuilder daylightFlow = createODLFlow(client, msg, flowKey, flowId, tableId);

        final InstanceIdentifier<Flow> flowPath = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(client.getNode().getId())).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableId)).child(Flow.class, flowKey).build();
        final InstanceIdentifier<Table> tableInstanceId = flowPath.<Table> firstIdentifierOf(Table.class);
        final InstanceIdentifier<Node> nodeInstanceId = flowPath.<Node> firstIdentifierOf(Node.class);

        final Flow odlFlow = daylightFlow.build();
        final AddFlowInputBuilder builder = new AddFlowInputBuilder(odlFlow);
        builder.setNode(new NodeRef(nodeInstanceId));
        builder.setFlowRef(new FlowRef(flowPath));
        builder.setFlowTable(new FlowTableRef(tableInstanceId));

        client.addFlow((AddFlowInput) builder.build());

    }

    private FlowBuilder createODLFlow(final Client client, final FlowModInput msg, final FlowKey flowKey,
            final FlowId flowId, final short tableId) {
        FlowBuilder allToCtrlFlow = new FlowBuilder().setTableId(tableId).setFlowName("OFProxy flow").setId(flowId)
                .setKey(flowKey);

        Match match = MatchConvertorImpl.fromOFMatchV10ToSALMatch(msg.getMatchV10(), client.getDatapathId(),
                OpenflowVersion.OF10);
        Instructions instructions = OFToMDSalFlowConvertor.wrapOF10ActionsToInstruction(msg.getAction(),
                OpenflowVersion.OF10);

        allToCtrlFlow.setCookie(new FlowCookie(msg.getCookie())).setMatch(match).setInstructions(instructions)
                .setPriority(msg.getPriority()).setBufferId(msg.getBufferId()).setHardTimeout(msg.getHardTimeout())
                .setIdleTimeout(msg.getIdleTimeout())
                .setFlags(new FlowModFlags(msg.getFlagsV10().isOFPFFCHECKOVERLAP(), false, // !msg.getFlowFlags().isByteCounts(),
                        false, // !msg.getFlowFlags().isPktCounts(),
                        false, // msg.getFlowFlags().isResetCounts(),
                        msg.getFlagsV10().isOFPFFSENDFLOWREM()));

        return allToCtrlFlow;
    }

}
