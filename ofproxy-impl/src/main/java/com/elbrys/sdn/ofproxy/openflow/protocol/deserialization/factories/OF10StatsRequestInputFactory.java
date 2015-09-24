/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.elbrys.sdn.ofproxy.openflow.protocol.deserialization.factories;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.desc._case.MultipartRequestDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table._case.MultipartRequestTableBuilder;

public final class OF10StatsRequestInputFactory implements OFDeserializer<MultipartRequestInput>,
        DeserializerRegistryInjector {

    private static final byte PADDING_IN_FLOW_STATS_HEADER = 1;
    private static final byte PADDING_IN_PORT_STATS_HEADER = 6;
    private static final byte PADDING_IN_QUEUE_HEADER = 2;
    private DeserializerRegistry registry;

    @Override
    public MultipartRequestInput deserialize(final ByteBuf rawMessage) {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        int type = rawMessage.readUnsignedShort();
        builder.setType(MultipartType.forValue(type));
        builder.setFlags(new MultipartRequestFlags((rawMessage.readUnsignedShort() & 0x01) != 0));
        switch (type) {
        case 0:
            builder.setMultipartRequestBody(setDesc(rawMessage));
            break;
        case 1:
            builder.setMultipartRequestBody(setFlow(rawMessage));
            break;
        case 2:
            builder.setMultipartRequestBody(setAggregate(rawMessage));
            break;
        case 3:
            builder.setMultipartRequestBody(setTable(rawMessage));
            break;
        case 4:
            builder.setMultipartRequestBody(setPortStats(rawMessage));
            break;
        case 5:
            builder.setMultipartRequestBody(setQueue(rawMessage));
            break;
        case 0xFFFF:
            builder.setMultipartRequestBody(setExperimenter(rawMessage));
            break;
        default:
            break;
        }
        return builder.build();
    }

    private static MultipartRequestDescCase setDesc(final ByteBuf input) {
        MultipartRequestDescCaseBuilder caseBuilder = new MultipartRequestDescCaseBuilder();
        MultipartRequestDescBuilder descBuilder = new MultipartRequestDescBuilder();
        caseBuilder.setMultipartRequestDesc(descBuilder.build());
        return caseBuilder.build();
    }

    private MultipartRequestFlowCase setFlow(final ByteBuf input) {
        MultipartRequestFlowCaseBuilder caseBuilder = new MultipartRequestFlowCaseBuilder();
        MultipartRequestFlowBuilder flowBuilder = new MultipartRequestFlowBuilder();
        OFDeserializer<MatchV10> matchDeserializer = registry.getDeserializer(new MessageCodeKey(
                EncodeConstants.OF10_VERSION_ID, EncodeConstants.EMPTY_VALUE, MatchV10.class));
        flowBuilder.setMatchV10(matchDeserializer.deserialize(input));
        flowBuilder.setTableId(input.readUnsignedByte());
        input.skipBytes(PADDING_IN_FLOW_STATS_HEADER);
        flowBuilder.setOutPort((long) input.readUnsignedShort());
        caseBuilder.setMultipartRequestFlow(flowBuilder.build());
        return caseBuilder.build();
    }

    private MultipartRequestAggregateCase setAggregate(final ByteBuf input) {
        MultipartRequestAggregateCaseBuilder caseBuilder = new MultipartRequestAggregateCaseBuilder();
        MultipartRequestAggregateBuilder builder = new MultipartRequestAggregateBuilder();
        OFDeserializer<MatchV10> matchDeserializer = registry.getDeserializer(new MessageCodeKey(
                EncodeConstants.OF10_VERSION_ID, EncodeConstants.EMPTY_VALUE, MatchV10.class));
        builder.setMatchV10(matchDeserializer.deserialize(input));
        builder.setTableId(input.readUnsignedByte());
        input.skipBytes(PADDING_IN_FLOW_STATS_HEADER);
        builder.setOutPort((long) input.readUnsignedShort());
        caseBuilder.setMultipartRequestAggregate(builder.build());
        return caseBuilder.build();
    }

    private MultipartRequestTableCase setTable(final ByteBuf input) {
        MultipartRequestTableCaseBuilder caseBuilder = new MultipartRequestTableCaseBuilder();
        MultipartRequestTableBuilder builder = new MultipartRequestTableBuilder();
        caseBuilder.setMultipartRequestTable(builder.build());
        return caseBuilder.build();
    }

    private MultipartRequestPortStatsCase setPortStats(final ByteBuf input) {
        MultipartRequestPortStatsCaseBuilder caseBuilder = new MultipartRequestPortStatsCaseBuilder();
        MultipartRequestPortStatsBuilder builder = new MultipartRequestPortStatsBuilder();
        builder.setPortNo((long) input.readUnsignedShort());
        input.skipBytes(PADDING_IN_PORT_STATS_HEADER);
        caseBuilder.setMultipartRequestPortStats(builder.build());
        return caseBuilder.build();
    }

    private MultipartRequestQueueCase setQueue(final ByteBuf input) {
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder builder = new MultipartRequestQueueBuilder();
        builder.setPortNo((long) input.readUnsignedShort());
        input.skipBytes(PADDING_IN_QUEUE_HEADER);
        builder.setQueueId(input.readUnsignedInt());
        caseBuilder.setMultipartRequestQueue(builder.build());
        return caseBuilder.build();
    }

    private MultipartRequestExperimenterCase setExperimenter(final ByteBuf input) {
        return registry.getDeserializer(createMultipartRequestMessageDeserializerKey(EncodeConstants.OF10_VERSION_ID,
                input.readUnsignedInt()));
    }

    private MessageCodeKey createMultipartRequestMessageDeserializerKey(final byte of10VersionId,
            final long experimenterId) {
        return new ExperimenterIdDeserializerKey(of10VersionId, experimenterId, MultipartRequestExperimenterCase.class);
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
