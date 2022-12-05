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

package de.fraunhofer.isst.edc.auditlogging;

import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.contract.spi.offer.store.ContractDefinitionStore;
import org.eclipse.edc.connector.policy.spi.store.PolicyDefinitionStore;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.event.EventRouter;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

public class AuditLoggingExtension implements ServiceExtension{
    
    @Inject
    private AuditLoggingManagerService auditManager;

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


    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        var config = context.getConfig();
        
        eventRouter.register(new AuditLoggingSubscriber(auditManager, monitor, 
            transferProcessStore,contractDefinitionStore,contractNegotiationStore,
            policyDefinitionStore, config));
        monitor.info("Initialized AuditLogging");
    }
}
