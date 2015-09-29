package com.elbrys.sdn.ofproxy.openflow.handlers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.OpendaylightQueueStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.OFProxy;
import com.elbrys.sdn.ofproxy.openflow.Client;
import com.elbrys.sdn.ofproxy.openflow.ClientNode;

public final class OF10StatsRequestHandler {
    private final Logger LOG = LoggerFactory.getLogger(OF10StatsRequestHandler.class);
    private final long OFPP_NONE = 0xffffl;

    private OpendaylightFlowStatisticsService flowService;
    private OpendaylightFlowTableStatisticsService tableService;
    private OpendaylightPortStatisticsService portService;
    private OpendaylightQueueStatisticsService queueService;

    public OF10StatsRequestHandler(final ConsumerContext sess) {
        flowService = sess.getRpcService(OpendaylightFlowStatisticsService.class);
        tableService = sess.getRpcService(OpendaylightFlowTableStatisticsService.class);
        portService = sess.getRpcService(OpendaylightPortStatisticsService.class);
        queueService = sess.getRpcService(OpendaylightQueueStatisticsService.class);
    }

    public void consume(final OFClientMsg msg) {
        if (!(msg.getMsg() instanceof MultipartRequestInput)) {
            LOG.warn("Invalid messare received. {}", msg.getMsg());
            return;
        }
        Client client = msg.getClient();
        MultipartRequestInput message = (MultipartRequestInput) msg.getMsg();
        MultipartReplyBody reply = null;
        if (message.getMultipartRequestBody() instanceof MultipartRequestDescCase) {
            reply = onDescRequest(client, message);
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestFlowCase) {
            reply = onFlowRequest(client, message);
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestAggregateCase) {
            reply = onAggregateRequest(client, message);
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestTableCase) {
            reply = onTableRequest(client, message);
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestPortStatsCase) {
            reply = onPortRequest(client, message);
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestQueueCase) {
            reply = onQueueRequest(client, message);
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestExperimenterCase) {
            reply = onExperimenterRequest(client, message);
        }

        if (reply == null) {
            LOG.warn("Unable to reply on {}", message.getMultipartRequestBody());
            return;
        }

        MultipartReplyMessage mrm = buildMultipartReplyMessage(message.getType(), client.getXid(), message.getFlags(),
                reply);

        client.send(mrm);

    }

    private MultipartReplyMessage buildMultipartReplyMessage(final MultipartType multipartType, final long xid,
            final MultipartRequestFlags flags, final MultipartReplyBody body) {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(xid);
        builder.setType(multipartType);
        // LOG.warn("Build msg tyope  {}", multipartType);
        builder.setFlags(flags);
        builder.setMultipartReplyBody(body);
        return builder.build();
    }

    private MultipartReplyBody onDescRequest(final Client client, final MultipartRequestInput message) {
        FlowCapableNode fcn = client.getFlowCapableNode();

        MultipartReplyDescCaseBuilder caseBuilder = new MultipartReplyDescCaseBuilder();
        MultipartReplyDescBuilder descBuilder = new MultipartReplyDescBuilder();
        if (fcn != null) {
            descBuilder.setMfrDesc(fcn.getManufacturer());
            descBuilder.setHwDesc(fcn.getHardware());
            descBuilder.setSwDesc(fcn.getSoftware());
            descBuilder.setSerialNum(fcn.getSerialNumber());
            descBuilder.setDpDesc(fcn.getDescription());
        }
        caseBuilder.setMultipartReplyDesc(descBuilder.build());
        return caseBuilder.build();
    }

    private MultipartReplyBody onFlowRequest(final Client client, final MultipartRequestInput message) {
        MultipartRequestFlowCase mrfc = (MultipartRequestFlowCase) message.getMultipartRequestBody();
        MultipartRequestFlow mrf = mrfc.getMultipartRequestFlow();

        GetAllFlowsStatisticsFromAllFlowTablesInputBuilder fsInput = new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder();
        fsInput.setNode(new NodeRef(client.getNodePath()));
        Future<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> future = flowService
                .getAllFlowsStatisticsFromAllFlowTables(fsInput.build());
        RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput> res = null;
        try {
            res = future.get(1000, TimeUnit.SECONDS);
            if (!res.isSuccessful()) {
                return null;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }

        GetAllFlowsStatisticsFromAllFlowTablesOutput fss = res.getResult();
        if (fss == null || fss.getFlowAndStatisticsMapList() == null) {
            return null;
        }

        List<FlowStats> retVal = new ArrayList<FlowStats>();
        for (FlowAndStatisticsMapList fs : fss.getFlowAndStatisticsMapList()) {
            if (mrf.getTableId() != 0xff && mrf.getTableId() != fs.getTableId()) {
                // skip record. table is not requested.
                continue;
            }
            if (mrf.getOutPort() != 0xffff && mrf.getOutPort() != fs.getOutPort().longValue()) {
                // skip record. outPort is out of range
                continue;
            }
            // fs.getMatch();
            // mrf.getMatchV10();
            // TODO COmpare Match vs Match V10 and skip flow stats if necessary
            retVal.add(convertToFlowStats(client, fs));
        }

        MultipartReplyFlowCaseBuilder caseBuilder = new MultipartReplyFlowCaseBuilder();
        MultipartReplyFlowBuilder flowBuilder = new MultipartReplyFlowBuilder();
        flowBuilder.setFlowStats(retVal);
        caseBuilder.setMultipartReplyFlow(flowBuilder.build());
        return caseBuilder.build();
    }

    private FlowStats convertToFlowStats(final Client client, final FlowAndStatisticsMapList fs) {
        FlowStatsBuilder fsb = new FlowStatsBuilder();
        fsb.setTableId(fs.getTableId());
        fsb.setMatchV10(convertToMatchV10(client, fs.getMatch()));
        fsb.setDurationSec(fs.getDuration().getSecond().getValue());
        fsb.setDurationNsec(fs.getDuration().getNanosecond().getValue());
        fsb.setPriority(fs.getPriority());
        fsb.setIdleTimeout(fs.getIdleTimeout());
        fsb.setHardTimeout(fs.getHardTimeout());
        fsb.setCookie(fs.getCookie().getValue());
        fsb.setPacketCount(fs.getPacketCount().getValue());
        fsb.setByteCount(fs.getByteCount().getValue());
        fsb.setAction(convertToAction(fs.getInstructions()));
        return fsb.build();
    }

    private List<Action> convertToAction(final Instructions instructions) {
        List<Action> retVal = new ArrayList<Action>();
        // TODO Convert ODL Instructions to OF10 Action list
        // Standard ODL converter is not found
        return retVal;
    }

    private MatchV10 convertToMatchV10(final Client client, final Match match) {
        MatchV10Builder mb = new MatchV10Builder();
        MatchReactor.getInstance().convert(match, EncodeConstants.OF10_VERSION_ID, mb, client.getDatapathId());
        return mb.build();
    }

    private MultipartReplyBody onAggregateRequest(final Client client, final MultipartRequestInput message) {
        MultipartRequestAggregateCase mrac = (MultipartRequestAggregateCase) message.getMultipartRequestBody();
        MultipartRequestAggregate mra = mrac.getMultipartRequestAggregate();

        GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder gsb = new GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder();
        gsb.setNode(new NodeRef(client.getNodePath()));
        gsb.setTableId(new TableId(mra.getTableId()));

        Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> future = flowService
                .getAggregateFlowStatisticsFromFlowTableForAllFlows(gsb.build());
        RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput> res;
        try {
            res = future.get(1000, TimeUnit.SECONDS);
            if (!res.isSuccessful()) {
                return null;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }

        GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput fss = res.getResult();
        if (fss == null) {
            return null;
        }

        MultipartReplyAggregateCaseBuilder caseBuilder = new MultipartReplyAggregateCaseBuilder();
        MultipartReplyAggregateBuilder aggrBuilder = new MultipartReplyAggregateBuilder();
        if (fss.getByteCount() != null) {
            aggrBuilder.setByteCount(fss.getByteCount().getValue());
        } else {
            aggrBuilder.setByteCount(BigInteger.valueOf(0));
        }
        if (fss.getPacketCount() != null) {
            aggrBuilder.setPacketCount(fss.getPacketCount().getValue());
        } else {
            aggrBuilder.setPacketCount(BigInteger.valueOf(0));
        }
        if (fss.getFlowCount() != null) {
            aggrBuilder.setFlowCount(fss.getFlowCount().getValue());
        } else {
            aggrBuilder.setFlowCount((long) 0);
        }
        caseBuilder.setMultipartReplyAggregate(aggrBuilder.build());
        return caseBuilder.build();
    }

    private MultipartReplyBody onTableRequest(final Client client, final MultipartRequestInput message) {
        GetFlowTablesStatisticsInputBuilder tfsb = new GetFlowTablesStatisticsInputBuilder();
        tfsb.setNode(new NodeRef(client.getNodePath()));
        Future<RpcResult<GetFlowTablesStatisticsOutput>> future = tableService.getFlowTablesStatistics(tfsb.build());
        RpcResult<GetFlowTablesStatisticsOutput> res;
        try {
            res = future.get(1000, TimeUnit.SECONDS);
            if (res.isSuccessful() && res.getResult() != null) {
                MultipartReplyTableCaseBuilder caseBuilder = new MultipartReplyTableCaseBuilder();
                MultipartReplyTableBuilder tableBuilder = new MultipartReplyTableBuilder();
                GetFlowTablesStatisticsOutput ftso = res.getResult();
                List<TableStats> retVal = new ArrayList<TableStats>();
                List<FlowTableAndStatisticsMap> ftsml = ftso.getFlowTableAndStatisticsMap();
                if (ftsml != null) {
                    for (FlowTableAndStatisticsMap ftsm : ftsml) {
                        TableStatsBuilder tsb = new TableStatsBuilder();
                        tsb.setTableId(ftsm.getTableId().getValue());
                        tsb.setName(ftsm.getTableId().toString());
                        // TODO get the following information from ODL
                        // tsb.setMaxEntries();
                        tsb.setMaxEntries((long) 256);
                        // tsb.setWildcards();
                        FlowWildcardsV10 fw = new FlowWildcardsV10(true, true, true, true, true, true, true, true,
                                true, true);
                        tsb.setWildcards(fw);
                        tsb.setActiveCount(ftsm.getActiveFlows().getValue());
                        tsb.setLookupCount(ftsm.getPacketsLookedUp().getValue());
                        tsb.setMatchedCount(ftsm.getPacketsMatched().getValue());
                        retVal.add(tsb.build());
                    }
                }
                tableBuilder.setTableStats(retVal);
                caseBuilder.setMultipartReplyTable(tableBuilder.build());
                return caseBuilder.build();
            } else {
                LOG.warn("Unable to get table statistics");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.warn("Unable to get table statistics", e);
        }
        return null;
    }

    private MultipartReplyBody onPortRequest(final Client client, final MultipartRequestInput message) {
        MultipartRequestPortStatsCase mrpc = (MultipartRequestPortStatsCase) message.getMultipartRequestBody();
        MultipartRequestPortStats mrp = mrpc.getMultipartRequestPortStats();

        GetAllNodeConnectorsStatisticsInputBuilder ncsib = new GetAllNodeConnectorsStatisticsInputBuilder();
        ncsib.setNode(new NodeRef(client.getNodePath()));
        Future<RpcResult<GetAllNodeConnectorsStatisticsOutput>> future = portService
                .getAllNodeConnectorsStatistics(ncsib.build());

        RpcResult<GetAllNodeConnectorsStatisticsOutput> res;
        try {
            res = future.get(1000, TimeUnit.SECONDS);
            if (res.isSuccessful()) {
                MultipartReplyPortStatsCaseBuilder caseBuilder = new MultipartReplyPortStatsCaseBuilder();
                MultipartReplyPortStatsBuilder portBuilder = new MultipartReplyPortStatsBuilder();
                GetAllNodeConnectorsStatisticsOutput ncso = res.getResult();
                List<PortStats> retVal = new ArrayList<PortStats>();
                for (NodeConnectorStatisticsAndPortNumberMap psm : ncso.getNodeConnectorStatisticsAndPortNumberMap()) {
                    FlowCapableNodeConnector fcnc = getNodeConnectorByNodeId(client, psm.getNodeConnectorId());
                    long portNo = fcnc.getPortNumber().getUint32();
                    if (mrp.getPortNo() == portNo || mrp.getPortNo() == OFPP_NONE) {
                        PortStatsBuilder psb = new PortStatsBuilder();
                        psb.setPortNo(portNo);
                        psb.setRxPackets(psb.getRxPackets());
                        psb.setTxPackets(psb.getTxPackets());
                        psb.setRxBytes(psb.getRxBytes());
                        psb.setTxBytes(psb.getTxBytes());
                        psb.setRxDropped(psb.getRxDropped());
                        psb.setTxDropped(psb.getTxDropped());
                        psb.setRxErrors(psb.getRxErrors());
                        psb.setTxErrors(psb.getTxErrors());
                        psb.setRxFrameErr(psb.getRxFrameErr());
                        psb.setRxOverErr(psb.getRxOverErr());
                        psb.setRxCrcErr(psb.getRxCrcErr());
                        psb.setCollisions(psb.getCollisions());
                        retVal.add(psb.build());
                    }
                }
                portBuilder.setPortStats(retVal);
                caseBuilder.setMultipartReplyPortStats(portBuilder.build());
                return caseBuilder.build();
            } else {
                LOG.warn("Unable to get table statistics");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.warn("Unable to getport statistics", e);
        }
        return null;
    }

    private FlowCapableNodeConnector getNodeConnectorByNodeId(final Client client, final NodeConnectorId nodeConnectorId) {
        NodeConnectorKey nConKey = new NodeConnectorKey(nodeConnectorId);
        InstanceIdentifier<NodeConnector> path = client.getNodePath().child(NodeConnector.class, nConKey).builder()
                .toInstance();
        NodeConnector nc = OFProxy.getConfigObject(path);
        FlowCapableNodeConnector fcnc = nc.getAugmentation(FlowCapableNodeConnector.class);
        return fcnc;
    }

    private MultipartReplyBody onQueueRequest(final Client client, final MultipartRequestInput message) {
        MultipartRequestQueueCase mrqc = (MultipartRequestQueueCase) message.getMultipartRequestBody();
        MultipartRequestQueue mrq = mrqc.getMultipartRequestQueue();

        GetQueueStatisticsFromGivenPortInputBuilder qsInput = new GetQueueStatisticsFromGivenPortInputBuilder();
        qsInput.setNode(new NodeRef(client.getNodePath()));
        NodeConnectorId ncId = client.getNodeConnectorIdByPortNumber(mrq.getPortNo());
        if (ncId == null) {
            return null;
        }
        qsInput.setNodeConnectorId(ncId);
        qsInput.setQueueId(new QueueId(mrq.getQueueId()));
        Future<RpcResult<GetQueueStatisticsFromGivenPortOutput>> future = queueService
                .getQueueStatisticsFromGivenPort(qsInput.build());
        RpcResult<GetQueueStatisticsFromGivenPortOutput> res = null;
        try {
            res = future.get(1000, TimeUnit.SECONDS);
            if (!res.isSuccessful()) {
                return null;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }

        GetQueueStatisticsFromGivenPortOutput fss = res.getResult();
        if (fss == null || fss.getQueueIdAndStatisticsMap() == null) {
            return null;
        }

        List<QueueStats> retVal = new ArrayList<QueueStats>();
        for (QueueIdAndStatisticsMap qs : fss.getQueueIdAndStatisticsMap()) {
            QueueStatsBuilder qsb = new QueueStatsBuilder();
            qsb.setDurationSec(qs.getDuration().getSecond().getValue());
            qsb.setDurationNsec(qs.getDuration().getNanosecond().getValue());
            qsb.setPortNo(ClientNode.getPortNfromNodeConnectorId(qs.getNodeConnectorId()));
            qsb.setQueueId(qs.getQueueId().getValue());
            qsb.setTxBytes(qs.getTransmittedBytes().getValue());
            qsb.setTxPackets(qs.getTransmittedPackets().getValue());
            qsb.setTxErrors(qs.getTransmissionErrors().getValue());
            retVal.add(qsb.build());
        }
        MultipartReplyQueueCaseBuilder caseBuilder = new MultipartReplyQueueCaseBuilder();
        MultipartReplyQueueBuilder queueBuilder = new MultipartReplyQueueBuilder();
        queueBuilder.setQueueStats(retVal);
        caseBuilder.setMultipartReplyQueue(queueBuilder.build());
        return caseBuilder.build();
    }

    private MultipartReplyBody onExperimenterRequest(final Client client, final MultipartRequestInput message) {
        LOG.error("Stats reply on Experimenter stats request is not implemented yet.");
        return null;
    }

}
