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

package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ofproxyapp.impl.rev150617;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ofproxyapp.api.rev150924.OfproxyappService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elbrys.sdn.OFPRoxyApp;

public class OFProxyappModule
        extends
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ofproxyapp.impl.rev150617.AbstractOFProxyappModule {

    private static final Logger LOG = LoggerFactory.getLogger(OFProxyappModule.class);

    public OFProxyappModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public OFProxyappModule(
            org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ofproxyapp.impl.rev150617.OFProxyappModule oldModule,
            java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("OFProxyappModule Create Instance.");
        final OFPRoxyApp ofProxyApp = new OFPRoxyApp();

        // To test RPC call
        // http://localhost:8181/apidoc/explorer/index.html
        // goto ofproxyapp and post 
        // {"ofproxyapp:input":{"datapathId":"101","controllerIp":"1.1.1.1","controllerPort":"101" }}

        LOG.info("OFProxyappModule Regiser RPC.");
        @SuppressWarnings("unused")
        final BindingAwareBroker.RpcRegistration<OfproxyappService> rpcRegistration = getRpcRegistryDependency()
                .addRpcImplementation(OfproxyappService.class, ofProxyApp);

        getBrokerDependency().registerConsumer(ofProxyApp, null);

        // Wrap proxyApp as AutoCloseable and close registrations to md-sal at
        // close(). The close method is where you would generally clean up
        // thread pools
        // etc.
        final class AutoCloseableOFProxyApp implements AutoCloseable {

            @Override
            public void close() throws Exception {
                ofProxyApp.close();
            }
        }
        return new AutoCloseableOFProxyApp();
    }

}
