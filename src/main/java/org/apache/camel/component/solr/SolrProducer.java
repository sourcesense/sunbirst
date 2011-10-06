/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.solr;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The Solr producer.
 */
public class SolrProducer extends DefaultProducer {
    private static final transient Logger LOG = LoggerFactory.getLogger(SolrProducer.class);
    private SolrServer solrServer;

    public SolrProducer(SolrEndpoint endpoint) {
        super(endpoint);
        solrServer = endpoint.getSolrServer();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        if (extractOperation(exchange).equals(SolrHeaders.COMMIT)) {
            solrServer.commit();
        } else {
            SolrInputDocument doc = new SolrInputDocument();
            for (Map.Entry<String, Object> entry : exchange.getIn().getHeaders().entrySet()) {
                if (entry.getKey().startsWith(SolrHeaders.FIELD)) {
                    String fieldName = entry.getKey().substring(SolrHeaders.FIELD.length());
                    doc.setField(fieldName, entry.getValue());
                }
            }
            solrServer.add(doc);
        }
    }

    private String extractOperation(Exchange exchange) {
        String operation = (String) exchange.getIn().getHeader(SolrHeaders.OPERATION);
        return (operation == null) ? SolrHeaders.INSERT : operation;
    }

    @Override
    public SolrEndpoint getEndpoint() {
        return (SolrEndpoint) super.getEndpoint();
    }

}
