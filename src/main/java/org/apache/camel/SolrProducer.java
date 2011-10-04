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
package org.apache.camel;

import org.apache.camel.impl.DefaultProducer;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * The Solr producer.
 */
public class SolrProducer extends DefaultProducer {
    private static final transient Logger LOG = LoggerFactory.getLogger(SolrProducer.class);
    private SolrEndpoint endpoint;


    private static final Logger Log = LoggerFactory.getLogger(SolrProducer.class);

    private SolrServer solrServer;

    public SolrProducer(SolrEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public SolrProducer(Endpoint endpoint, String address) {
        super(endpoint);

        try {
            solrServer = new CommonsHttpSolrServer(address);
        } catch (MalformedURLException muex) {
            log.error("SEVERE: Could not connect to Solr server: {}", address);
            // TODO: Put exception on Exchange.
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        // TODO.
        if (solrServer == null) throw new Exception();

        // TODO: This doesn't actually convert yet. Need to write a converter.
        //       Most likely, the message will be an internal Document of some
        //       sort, which will then be transformed into a SolrInputDocument.

        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("text", exchange.getIn().getBody(String.class));

        for (Map.Entry<String, Object> entry : exchange.getIn().getHeaders().entrySet())  {
            if (entry.getKey().startsWith("solr.field.")) {
                String fieldName = entry.getKey().substring(11);
                doc.setField(fieldName, entry.getValue());
            }
        }

        solrServer.add(doc);
        solrServer.commit();
    }

    @Override
    // TODO: What does this do?
    public SolrEndpoint getEndpoint() {
        return (SolrEndpoint) super.getEndpoint();
    }

}
