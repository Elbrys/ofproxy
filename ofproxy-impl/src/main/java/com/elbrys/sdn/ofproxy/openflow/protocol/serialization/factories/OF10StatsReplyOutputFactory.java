/*
 * Copyright (c) 2015,  BROCADE COMMUNICATIONS SYSTEMS, INC
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.elbrys.sdn.ofproxy.openflow.protocol.serialization.factories;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.extensibility.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ListSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.TypeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.TypeKeyMakerFactory;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowjava.util.ExperimenterSerializerKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdMultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.experimenter._case.MultipartReplyExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;

public final class OF10StatsReplyOutputFactory implements OFSerializer<MultipartReplyMessage>,
        SerializerRegistryInjector {
    private static final byte MESSAGE_TYPE = 17;
    private static final TypeKeyMaker<Action> ACTION_KEY_MAKER = TypeKeyMakerFactory
            .createActionKeyMaker(EncodeConstants.OF10_VERSION_ID);

    private static final int DESC_STR_LEN = 256;
    private static final int SERIAL_NUM_LEN = 32;
    private static final int OFP_MAX_TABLE_NAME_LEN = 32;
    private static final byte PADDING_IN_MULTIPART_REPLY_TABLE_BODY = 3;
    private static final byte PADDING_IN_MULTIPART_REPLY_FLOW_BODY_1 = 1;
    private static final byte PADDING_IN_MULTIPART_REPLY_FLOW_BODY_2 = 6;
    private static final byte PADDING_IN_MULTIPART_REPLY_AGGREGATE_BODY = 4;
    private static final byte PADDING_IN_MULTIPART_REPLY_PORT_BODY = 6;
    private static final byte PADING_IN_QUEUE_BODY = 2;

    private SerializerRegistry registry;

    @Override
    public void serialize(final MultipartReplyMessage message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.OFHEADER_SIZE);
        outBuffer.writeShort(message.getType().getIntValue());
        outBuffer.writeShort(createMultipartReplyFlagsBitmask(message.getFlags()));
        if (message.getMultipartReplyBody() instanceof MultipartReplyDescCase) {
            serializeDescBody(message.getMultipartReplyBody(), outBuffer);
        } else if (message.getMultipartReplyBody() instanceof MultipartReplyFlowCase) {
            serializeFlowBody(message.getMultipartReplyBody(), outBuffer);
        } else if (message.getMultipartReplyBody() instanceof MultipartReplyAggregateCase) {
            serializeAggregateBody(message.getMultipartReplyBody(), outBuffer);
        } else if (message.getMultipartReplyBody() instanceof MultipartReplyTableCase) {
            serializeTableBody(message.getMultipartReplyBody(), outBuffer);
        } else if (message.getMultipartReplyBody() instanceof MultipartReplyPortStatsCase) {
            serializePortBody(message.getMultipartReplyBody(), outBuffer);
        } else if (message.getMultipartReplyBody() instanceof MultipartReplyQueueCase) {
            serializeQueueBody(message.getMultipartReplyBody(), outBuffer);
        } else if (message.getMultipartReplyBody() instanceof MultipartReplyExperimenterCase) {
            serializeExperimenterBody(message.getMultipartReplyBody(), outBuffer);
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private static int createMultipartReplyFlagsBitmask(final MultipartRequestFlags flags) {
        return ByteBufUtils.fillBitMask(0, flags.isOFPMPFREQMORE());
    }

    private static void createStringBytes(final ByteBuf outBuffer, final String str, final int length) {
        // Name should be saved as 16 bit null terminated string
        String trim = "";
        if (str != null) {
            trim = str.substring(0, Math.min(length, str.length()));
        }
        outBuffer.writeBytes(trim.getBytes());
        outBuffer.writeZero(length - trim.getBytes().length);
    }

    /**
     * @param multipartReplyBody
     * @param output
     */
    private void serializeDescBody(final MultipartReplyBody multipartReplyBody, final ByteBuf output) {
        MultipartReplyDescCase descCase = (MultipartReplyDescCase) multipartReplyBody;
        MultipartReplyDesc desc = descCase.getMultipartReplyDesc();
        createStringBytes(output, desc.getMfrDesc(), DESC_STR_LEN);
        createStringBytes(output, desc.getHwDesc(), DESC_STR_LEN);
        createStringBytes(output, desc.getSwDesc(), DESC_STR_LEN);
        createStringBytes(output, desc.getSerialNum(), SERIAL_NUM_LEN);
        createStringBytes(output, desc.getDpDesc(), DESC_STR_LEN);
    }

    /**
     * @param multipartReplyBody
     * @param out
     */
    private void serializeTableBody(final MultipartReplyBody multipartReplyBody, final ByteBuf output) {
        MultipartReplyTableCase tableCase = (MultipartReplyTableCase) multipartReplyBody;
        MultipartReplyTable table = tableCase.getMultipartReplyTable();
        for (TableStats ts : table.getTableStats()) {
            output.writeByte(ts.getTableId());
            output.writeZero(PADDING_IN_MULTIPART_REPLY_TABLE_BODY);
            createStringBytes(output, ts.getTableId().toString(), OFP_MAX_TABLE_NAME_LEN);
            output.writeInt(createWildcardsInt(ts.getWildcards()));
            output.writeInt(ts.getMaxEntries().intValue());
            output.writeInt(ts.getActiveCount().intValue());
            output.writeLong(ts.getLookupCount().longValue());
            output.writeLong(ts.getMatchedCount().longValue());
        }
    }

    public static int createWildcardsInt(final FlowWildcardsV10 input) {
        return ByteBufUtils.fillBitMask(0, input.isINPORT().booleanValue(), input.isDLVLAN().booleanValue(), input
                .isDLSRC().booleanValue(), input.isDLDST().booleanValue(), input.isDLTYPE().booleanValue(), input
                .isNWPROTO().booleanValue(), input.isTPSRC().booleanValue(), input.isTPDST().booleanValue(),// 7
                false, false, false, false, false, false, false, false, false, false, false, false, input.isDLVLANPCP()
                        .booleanValue(),// 20
                input.isNWTOS().booleanValue());// 21
    }

    private void serializeFlowBody(final MultipartReplyBody multipartReplyBody, final ByteBuf output) {
        MultipartReplyFlowCase flowCase = (MultipartReplyFlowCase) multipartReplyBody;
        MultipartReplyFlow flows = flowCase.getMultipartReplyFlow();
        for (FlowStats fs : flows.getFlowStats()) {
            int flowStartIndex = output.readableBytes();
            output.writeShort(0);// Flow lenght. To be recorded at the end
            output.writeByte(fs.getTableId().byteValue());
            output.writeZero(PADDING_IN_MULTIPART_REPLY_FLOW_BODY_1);
            OFSerializer<MatchV10> matchSerializer = registry.getSerializer(new MessageTypeKey<>(
                    EncodeConstants.OF10_VERSION_ID, MatchV10.class));
            matchSerializer.serialize(fs.getMatchV10(), output);
            output.writeLong(fs.getDurationSec());
            output.writeLong(fs.getDurationNsec());
            output.writeInt(fs.getPriority());
            output.writeInt(fs.getIdleTimeout());
            output.writeInt(fs.getHardTimeout());
            output.writeZero(PADDING_IN_MULTIPART_REPLY_FLOW_BODY_2);
            output.writeLong(fs.getCookie().longValue());
            output.writeLong(fs.getPacketCount().longValue());
            output.writeLong(fs.getByteCount().longValue());
            ListSerializer.serializeList(fs.getAction(), ACTION_KEY_MAKER, registry, output);
            output.setShort(flowStartIndex, output.readableBytes() - flowStartIndex);
        }
    }

    private void serializeAggregateBody(final MultipartReplyBody multipartReplyBody, final ByteBuf output) {
        MultipartReplyAggregateCase aggrCase = (MultipartReplyAggregateCase) multipartReplyBody;
        MultipartReplyAggregate aggr = aggrCase.getMultipartReplyAggregate();
        output.writeLong(aggr.getPacketCount().longValue());
        output.writeLong(aggr.getByteCount().longValue());
        output.writeInt(aggr.getFlowCount().intValue());
        output.writeZero(PADDING_IN_MULTIPART_REPLY_AGGREGATE_BODY);
    }

    private static void serializePortBody(final MultipartReplyBody multipartReplyBody, final ByteBuf output) {
        MultipartReplyPortStatsCase portCase = (MultipartReplyPortStatsCase) multipartReplyBody;
        MultipartReplyPortStats ports = portCase.getMultipartReplyPortStats();
        for (PortStats ps : ports.getPortStats()) {
            output.writeShort(ps.getPortNo().shortValue());
            output.writeZero(PADDING_IN_MULTIPART_REPLY_PORT_BODY);
            output.writeLong(ps.getRxPackets().longValue());
            output.writeLong(ps.getTxPackets().longValue());
            output.writeLong(ps.getRxBytes().longValue());
            output.writeLong(ps.getTxBytes().longValue());
            output.writeLong(ps.getRxDropped().longValue());
            output.writeLong(ps.getTxDropped().longValue());
            output.writeLong(ps.getRxErrors().longValue());
            output.writeLong(ps.getTxErrors().longValue());
            output.writeLong(ps.getRxFrameErr().longValue());
            output.writeLong(ps.getRxOverErr().longValue());
            output.writeLong(ps.getRxCrcErr().longValue());
            output.writeLong(ps.getCollisions().longValue());
        }
    }

    private static void serializeQueueBody(final MultipartReplyBody multipartReplyBody, final ByteBuf output) {
        MultipartReplyQueueCase queueCase = (MultipartReplyQueueCase) multipartReplyBody;
        MultipartReplyQueue queues = queueCase.getMultipartReplyQueue();
        for (QueueStats qs : queues.getQueueStats()) {
            output.writeShort(qs.getPortNo().shortValue());
            output.writeZero(PADING_IN_QUEUE_BODY);
            output.writeInt(qs.getQueueId().intValue());
            output.writeLong(qs.getTxBytes().longValue());
            output.writeLong(qs.getTxPackets().longValue());
            output.writeLong(qs.getTxErrors().longValue());
        }
    }

    private void serializeExperimenterBody(final MultipartReplyBody multipartReplyBody, final ByteBuf output) {
        MultipartReplyExperimenterCase expCase = (MultipartReplyExperimenterCase) multipartReplyBody;
        MultipartReplyExperimenter experimenter = expCase.getMultipartReplyExperimenter();
        long expId = experimenter.getAugmentation(ExperimenterIdMultipartReply.class).getExperimenter().getValue();
        OFSerializer<MultipartReplyExperimenterCase> serializer = registry
                .getSerializer(ExperimenterSerializerKeyFactory.createExperimenterMessageSerializerKey(
                        EncodeConstants.OF10_VERSION_ID, expId));
        serializer.serialize(expCase, output);
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        this.registry = serializerRegistry;
    }
}