/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.agent.monitor.extension;

import org.hawkular.agent.monitor.extension.MonitorServiceConfiguration.EndpointConfiguration;
import org.hawkular.agent.monitor.log.AgentLoggers;
import org.hawkular.agent.monitor.log.MsgLogger;
import org.hawkular.agent.monitor.protocol.EndpointService;
import org.hawkular.agent.monitor.protocol.ProtocolService;
import org.hawkular.agent.monitor.protocol.ProtocolServices;
import org.hawkular.agent.monitor.protocol.jmx.JMXNodeLocation;
import org.hawkular.agent.monitor.protocol.jmx.JMXSession;
import org.hawkular.agent.monitor.service.MonitorService;
import org.hawkular.agent.monitor.util.Util;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

public class LocalJMXAdd extends MonitorServiceAddStepHandler {
    private static final MsgLogger log = AgentLoggers.getLogger(LocalJMXAdd.class);

    public static final LocalJMXAdd INSTANCE = new LocalJMXAdd();

    private LocalJMXAdd() {
        super(LocalJMXAttributes.ATTRIBUTES);
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model)
            throws OperationFailedException {

        if (context.isBooting()) {
            return;
        }

        MonitorService monitorService = getMonitorService(context);
        if (monitorService == null) {
            return; // the agent wasn't enabled, nothing to do
        }

        MonitorServiceConfiguration config = Util.getMonitorServiceConfiguration(context);
        String newEndpointName = context.getCurrentAddressValue();

        // Register the feed under the tenant of the new managed server.
        // If endpoint has a null tenant then there is nothing to do since it will just reuse the agent's tenant ID
        EndpointConfiguration endpointConfig = config.getJmxConfiguration().getEndpoints().get(newEndpointName);
        boolean isEnabled = endpointConfig.isEnabled();

        String newTenantId = endpointConfig.getTenantId();
        if (newTenantId != null) {
            try {
                monitorService.registerFeed(newTenantId, 0);
            } catch (Exception e) {
                isEnabled = false;
                log.warnCannotRegisterFeedForNewManagedServer(newTenantId, newEndpointName, e.toString());
            }
        }

        if (isEnabled) {
            // create a new endpoint service
            ProtocolServices newServices = monitorService.createProtocolServicesBuilder()
                    .jmxProtocolService(config.getJmxConfiguration()).build();
            EndpointService<JMXNodeLocation, JMXSession> endpointService = newServices.getJmxProtocolService()
                    .getEndpointServices().get(newEndpointName);

            // put the new endpoint service in the original protocol services container
            ProtocolService<JMXNodeLocation, JMXSession> jmxService = monitorService.getProtocolServices()
                    .getJmxProtocolService();
            jmxService.add(endpointService);
        }
    }
}
