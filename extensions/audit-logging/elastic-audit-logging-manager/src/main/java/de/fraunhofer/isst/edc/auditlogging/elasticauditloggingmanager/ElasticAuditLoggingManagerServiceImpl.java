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

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import de.fraunhofer.isst.edc.auditlogging.AuditLoggingManagerService;
import de.fraunhofer.isst.edc.auditlogging.Log;

import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.configuration.Config;
import org.elasticsearch.client.RestClient;

import java.io.*;

public class ElasticAuditLoggingManagerServiceImpl implements AuditLoggingManagerService {
    private Config elasticConfig;
    private Monitor monitor;

    private RestClient restClient;
    private ElasticsearchTransport transport;
    private ElasticsearchClient client;


    public ElasticAuditLoggingManagerServiceImpl(Config conf, Monitor monitor) {
        this.elasticConfig = conf;

        this.monitor = monitor;

        // Create the low-level client
        this.restClient = RestClient.builder(
            new HttpHost(elasticConfig.getString("url"), elasticConfig.getInteger("port"))).build();

        // Create the transport with a Jackson mapper
        this.transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // And create the API client
        this.client = new ElasticsearchClient(transport);
    }

    @Override
    public void addLog(Log log) {
        
        try {
            if (!client.indices().exists(new ExistsRequest.Builder().
                    index(elasticConfig.getString("indexprefix")).build()).value())
            {
                client.indices().create(c -> c.index(elasticConfig.getString("indexprefix")));
            }
            
            client.index(i -> i
                    .index(elasticConfig.getString("indexprefix"))
                    .document(log)
            );
        } catch (IOException e) {
            monitor.severe("Audit log could not be written to Elastic");
            monitor.severe("Check if Elasticsearch Server is available");
            monitor.info(log.toString());
        }
    }
}
