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
package com.sourcesense.sunbirst.component.solr;

import com.sourcesense.sunbirst.Document;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.AfterClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Simple Solr Endpoint testing.
 */
public class SolrEndpointTest extends CamelTestSupport
{
    private static JettySolrRunner solrRunner;
    private SolrServer solrServer;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Test
    public void addingSolrXmlShouldBeSuccessful() throws Exception {
        clearIndex();

        //Document document = new Document();
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", "MA147LL/A");
        document.addField("name", "Apple 60 GB iPod with Video Playback Black");
        document.addField("manu", "Apple Computer Inc.");
        document.addField("cat", "electronics");

        template.sendBody(document);

        // Check things were indexed.
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");
        QueryResponse response = solrServer.query(solrQuery);

        assertEquals(0, response.getStatus());
        assertEquals(1, response.getResults().getNumFound());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("solr://localhost:8983/solr");
            }
        };
    }

    @Override
    protected void doPreSetup() throws Exception {
        // Start a Solr instance.
        solrRunner = new JettySolrRunner("/solr", 8983);
        solrRunner.start();

        solrServer = new CommonsHttpSolrServer("http://localhost:8983/solr");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        solrRunner.stop();
    }

    private void clearIndex() throws Exception {
        // Clear the Solr index.
        solrServer.deleteByQuery("*:*");
        solrServer.commit();
    }
}
