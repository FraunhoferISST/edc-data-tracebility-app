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
import org.eclipse.edc.spi.event.Event;
import org.eclipse.edc.spi.event.EventSubscriber;
import org.eclipse.edc.spi.event.asset.AssetCreated;
import org.eclipse.edc.spi.event.asset.AssetDeleted;
import org.eclipse.edc.spi.event.asset.AssetEventPayload;
import org.eclipse.edc.spi.event.contractdefinition.ContractDefinitionCreated;
import org.eclipse.edc.spi.event.contractdefinition.ContractDefinitionDeleted;
import org.eclipse.edc.spi.event.contractdefinition.ContractDefinitionEventPayload;
import org.eclipse.edc.spi.event.contractnegotiation.ContractNegotiationEventPayload;
import org.eclipse.edc.spi.event.policydefinition.PolicyDefinitionCreated;
import org.eclipse.edc.spi.event.policydefinition.PolicyDefinitionDeleted;
import org.eclipse.edc.spi.event.policydefinition.PolicyDefinitionEventPayload;
import org.eclipse.edc.spi.event.transferprocess.TransferProcessEventPayload;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.configuration.Config;


public class AuditLoggingSubscriber implements EventSubscriber {

    private AuditLoggingManagerService auditLoggingManagerService;
    private Monitor monitor;

    private String edcHostID;

    private TransferProcessStore transferProcessStore;
    private ContractDefinitionStore contractDefinitionStore;
    private ContractNegotiationStore contractNegotiationStore;

    private Config config;

    public AuditLoggingSubscriber(AuditLoggingManagerService auditLoggingManagerService, Monitor monitor, TransferProcessStore transferProcessStore,
                                  ContractDefinitionStore contractDefinitionStore, ContractNegotiationStore contractNegotiationStore,
                                  PolicyDefinitionStore policyDefinitionStore, Config conf) {
        this.auditLoggingManagerService = auditLoggingManagerService;
        this.monitor = monitor;
        this.transferProcessStore = transferProcessStore;
        this.contractNegotiationStore = contractNegotiationStore;
        this.contractDefinitionStore = contractDefinitionStore;

        this.config = conf;

        edcHostID = config.getString("edc.hostname", "localhost"); //TODO Check Correct ID
    }

    @Override
    public void on(Event<?> event) {
        if (event.getPayload() instanceof TransferProcessEventPayload){
            var transferProcessEvent = transferProcessStore.find(((TransferProcessEventPayload) event.getPayload()).getTransferProcessId());
            var props = transferProcessEvent.getDataRequest().getDataDestination().getProperties();
            if (event.getClass().getSimpleName().equals("TransferProcessCompleted")) {
                if (props.containsKey("type")) {
                    if (props.get("type").toString().equals("AmazonS3")) {
                        String awsPrettyMsg = String.format("[AWSPut] The asset %s was added to the bucket %s at server %s with the key %s.",
                                transferProcessEvent.getDataRequest().getContractId(), props.get("bucketName"), props.get("region"), props.get("keyName"));
                        String awsMsg = String.format("{\"prettyMessage\" : \"%s\" , \"assetID\" : \"%s\" , \"awsID\" : \"%s\",\"type\" : \"AWSPut\"}",
                                awsPrettyMsg, transferProcessEvent.getDataRequest().getContractId(), props.get("keyName"));
                        Log awsLog = new Log(edcHostID, String.valueOf((int) (event.getAt() / 2)), awsMsg);
                        auditLoggingManagerService.addLog(awsLog);
                    }
                }
            }
        }

        String message = null;

        // [AssetEvent] #EDCID created asset ASSETID
        if (event.getPayload() instanceof AssetCreated.Payload) {
            var eventPayload = (AssetEventPayload) event.getPayload();
            message = String.format("{\"prettyMessage\": \"[AssetEvent] %s created asset %s\",\"type\" : \"AssetEvent\"}",
                    edcHostID,
                    eventPayload.getAssetId());
        }

        // [AssetEvent] #EDCID deleted asset ASSETID
        if (event.getPayload() instanceof AssetDeleted.Payload) {
            var eventPayload = (AssetEventPayload) event.getPayload();
            message = String.format("{\"prettyMessage\": \"[AssetEvent] %s deleted asset %s\",\"type\" : \"AssetEvent\"}",
                    edcHostID,
                    eventPayload.getAssetId());
        }

        // [PolicyEvent] #EDCID created policy #POLICYID
        if (event.getPayload() instanceof PolicyDefinitionCreated.Payload) {
            var eventPayload = (PolicyDefinitionEventPayload) event.getPayload();
            message = String.format("{\"prettyMessage\": \"[PolicyEvent] %s created policy %s\",\"type\" : \"PolicyEvent\"}",
                    edcHostID,
                    eventPayload.getPolicyDefinitionId());
        }
        // [PolicyEvent] #EDCID deleted policy #POLICYID
        if (event.getPayload() instanceof PolicyDefinitionDeleted.Payload) {
            var eventPayload = (PolicyDefinitionEventPayload) event.getPayload();
            message = String.format("{\"prettyMessage\": \"[PolicyEvent] %s deleted policy %s\",\"type\" : \"PolicyEvent\"}",
                    edcHostID,
                    eventPayload.getPolicyDefinitionId());
        }

        // [ContractDefinitionEvent] #EDCID created contractdefinition #CONTRACTDEFINITIONID. With this asset #ASSETID is published with accesspolicy #POLICYID and usepolicy #POLICYID.
        if (event.getPayload() instanceof ContractDefinitionCreated.Payload) {
            var eventPayload = (ContractDefinitionEventPayload) event.getPayload();
            var list = contractDefinitionStore.findById(eventPayload.getContractDefinitionId()).getSelectorExpression().getCriteria();
            String assetId = null;
            for (var ele : list) {
                if (ele.getOperandLeft().equals("asset:prop:id")) {
                    assetId = ele.getOperandRight().toString();
                }
            }
            message = String.format("{\"prettyMessage\": \"[ContractDefinitionEvent] %s created contractdefinition %s. " +
                            "With this asset %s is published with accesspolicy %s and usepolicy %s.\",\"type\" : \"ContractDefinitionEvent\"}",
                    edcHostID,
                    eventPayload.getContractDefinitionId(),
                    assetId,
                    contractDefinitionStore.findById(eventPayload.getContractDefinitionId()).getAccessPolicyId(),
                    contractDefinitionStore.findById(eventPayload.getContractDefinitionId()).getContractPolicyId());
        }

        // [ContractDefinitionEvent] #EDCID deleted contractdefinition #CONTRACTDEFINITIONID.
        if (event.getPayload() instanceof ContractDefinitionDeleted.Payload) {
            var eventPayload = (ContractDefinitionEventPayload) event.getPayload();
            message = String.format("{\"prettyMessage\": \"[ContractDefinitionEvent] %s deleted contractdefinition %s.\",\"type\" : \"ContractDefinitionEvent\"}",
                    edcHostID,
                    eventPayload.getContractDefinitionId());
        }

        // [ContractNegotiationEvent] #EDCIDConsumer is negotiating with #EDCIDProvider about the asset #assetId with policy #PolicyID. Status: [Approved|Confirmed|Declined|Failed|Initiated|Offered|Requested]
        if (event.getPayload() instanceof ContractNegotiationEventPayload) {
            var eventPayload = (ContractNegotiationEventPayload) event.getPayload();
            var contractNegotiation = contractNegotiationStore.find(eventPayload.getContractNegotiationId());
            message = String.format("{\"prettyMessage\": \"[ContractNegotiationEvent] %s is negotiating with %s about the asset %s with policy %s. Status: %s\",\"type\" : \"ContractNegotiationEvent\"}",
                    edcHostID,
                    contractNegotiation.getCounterPartyId() + ":" + contractNegotiation.getCounterPartyAddress(), //TODO Check if good
                    contractNegotiation.getLastContractOffer().getAsset().getId(),
                    contractNegotiation.getLastContractOffer().getId(),
                    event.getClass().getSimpleName());
        }

        // [TransferProcessEvent] #EDCIDConsumer is transferring an asset #ASSETID from #EDCProvider. Status: [Approved|Confirmed|Declined|Failed|Initiated|Offered|Requested]
        if (event.getPayload() instanceof TransferProcessEventPayload) {
            var eventPayload = (TransferProcessEventPayload) event.getPayload();
            var transferProcess = transferProcessStore.find(eventPayload.getTransferProcessId());
            message = String.format("{\"prettyMessage\": \"[TransferProcessEvent] %s is transferring an asset %s to %s. Status: %s\",\"type\" : \"TransferProcessEvent\"}",
                    transferProcess.getDataRequest().getConnectorId() + ":" + transferProcess.getDataRequest().getConnectorAddress(), //TODO Check if this is good
                    transferProcess.getDataRequest().getAssetId(),
                    transferProcess.getDataRequest().getDataDestination().getProperties().get("baseUrl"), //TODO CHECK IF THIS IS GOOD
                    event.getClass().getSimpleName());
        }

        monitor.info(message);


        if (message != null) {
            Log log = new Log(edcHostID, String.valueOf(event.getAt()), message);
            auditLoggingManagerService.addLog(log);
        } else {
            monitor.severe("Event couldnt be logged: " + event.getClass().getSimpleName());
        }
    }
}