package com.elbrys.sdn.ofproxy;

import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareConsumer;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * Main application activator class for registering the dependencies and
 * initializing the OFProxy application.
 * 
 */
public final class Activator extends AbstractBindingAwareConsumer implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    /** ConsumerContext session. */
    private static ConsumerContext session = null;

    private OFProxy ofProxy;

    @Override
    public void close() {
        LOG.info("Activator close()");
        if (ofProxy != null) {
            ofProxy.stop();
            ofProxy = null;
        }
    }

    @Override
    public void onSessionInitialized(final ConsumerContext sess) {
        Activator.session = sess;
        LOG.info("Activator onSessionInitialized()");
        ofProxy = new OFProxy(sess);
        LOG.info("Activator onSessionInitialized OFProxy constructed");
        ofProxy.start();
        LOG.info("Activator onSessionInitialized() finished.");
    }

    @Override
    protected void startImpl(final BundleContext context) {
        LOG.info("Activator startImpl()");
    }

    @Override
    protected void stopImpl(final BundleContext context) {
        close();
        super.stopImpl(context);
    }

    /**
     * Returns object from ODL operational data store.
     * 
     * @param objRef Object reference
     * @return Requested object
     */
    public static <K extends DataObject> K getConfigObject(final InstanceIdentifier<K> objRef) {
        ReadOnlyTransaction readTx = session.getSALService(DataBroker.class).newReadOnlyTransaction();
        Optional<K> data;
        try {
            data = readTx.read(LogicalDatastoreType.OPERATIONAL, objRef).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }

        if (!data.isPresent()) {
            return null;
        }

        return data.get();
    }
}
