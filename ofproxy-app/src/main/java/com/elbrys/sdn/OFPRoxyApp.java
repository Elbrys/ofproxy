package com.elbrys.sdn;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareConsumer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ofproxyapp.api.rev150924.OfproxyAddInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ofproxyapp.api.rev150924.OfproxyAddOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ofproxyapp.api.rev150924.OfproxyAddOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ofproxyapp.api.rev150924.OfproxyappService;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.ofproxy.OFProxy;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;

public class OFPRoxyApp implements BindingAwareConsumer, OfproxyappService {
    private static final Logger LOG = LoggerFactory.getLogger(OFPRoxyApp.class);

    private OFProxy ofProxy;
    
    public OFPRoxyApp() {
        LOG.info("OFPRoxyApp contructor");
    }

    @Override
    public Future<RpcResult<OfproxyAddOutput>> ofproxyAdd(OfproxyAddInput input) {
        LOG.info("ExampleAppImpl exampleRpc");
        // Build output message
        OfproxyAddOutputBuilder outBuilder = new OfproxyAddOutputBuilder();
        
        outBuilder.setResult(ofProxy.addConnection(input.getDatapathId(), input.getControllerIp(), input.getControllerPort()));

        return Futures.immediateFuture(RpcResultBuilder.success(outBuilder.build()).build());
    }

    public void close() {
        LOG.info("ExampleAppImpl close");
    }

    @Override
    public void onSessionInitialized(ConsumerContext context) {
        LOG.info("ExampleAppImpl onSessionInitialized");
        ofProxy = new OFProxy(context);
        ofProxy.start();
    }

}
