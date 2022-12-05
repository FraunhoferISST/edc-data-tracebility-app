/*
 *  Copyright (c) 2022 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package de.fraunhofer.isst.edc.auditlogging.elasticauditloggingmanager;

import de.fraunhofer.isst.edc.auditlogging.AuditLoggingManagerService;
import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.contract.spi.offer.store.ContractDefinitionStore;
import org.eclipse.edc.connector.policy.spi.store.PolicyDefinitionStore;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.spi.event.EventRouter;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

@Provides({ AuditLoggingManagerService.class })
public class ElasticAuditLoggingManagerExtension implements ServiceExtension {

    public static final String ELASTICSEARCH_LOGGING_CONFIG = "elasticsearch.logging";
    @Inject
    private EventRouter eventRouter;

    @Inject
    private TransferProcessStore transferProcessStore;

    @Inject
    private ContractNegotiationStore contractNegotiationStore;

    @Inject
    private ContractDefinitionStore contractDefinitionStore;

    @Inject
    private PolicyDefinitionStore policyDefinitionStore;

    @Inject
    private Monitor monitor;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var auditConfig = context.getConfig(ELASTICSEARCH_LOGGING_CONFIG);
        var auditManager = new ElasticAuditLoggingManagerServiceImpl(auditConfig, monitor);
        context.registerService(AuditLoggingManagerService.class, auditManager);
        monitor.info("ElasticSearch AuditLogging Service initialized");
    }
}