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
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

public class SolrComponentTest extends CamelTestSupport {

    private static JettySolrRunner solrRunner;
    private static CommonsHttpSolrServer solrServer;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Test
    public void addingSolrXmlShouldBeSuccessful() throws Exception {
        Exchange exchange = createExchangeWithBody("<hello>world!</hello>");
        exchange.getIn().setHeader("solr.field.id", "MA147LL/A");

        template.send(exchange);

        // Check things were indexed.
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");
        QueryResponse response = solrServer.query(solrQuery);

        assertEquals(0, response.getStatus());
        assertEquals(1, response.getResults().getNumFound());
    }

    @Test
    public void setHeadersAsSolrFields() throws Exception {
        Exchange exchange = createExchangeWithBody("Test body for iPod.");
        exchange.getIn().setHeader("solr.field.id", "MA147LL/A");
        exchange.getIn().setHeader("solr.field.name", "Apple 60 GB iPod with Video Playback Black");
        exchange.getIn().setHeader("solr.field.manu", "Apple Computer Inc.");
        template.send(exchange);

        // Check things were indexed.
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("id:MA147LL/A");
        QueryResponse response = solrServer.query(solrQuery);

        assertEquals(0, response.getStatus());
        assertEquals(1, response.getResults().getNumFound());

        SolrDocument doc = response.getResults().get(0);
        assertEquals("Apple 60 GB iPod with Video Playback Black", doc.getFieldValue("name"));
        assertEquals("Apple Computer Inc.", doc.getFieldValue("manu"));
    }

    @Test
    public void setMultiValuedFieldInHeader() throws Exception {
        String[] categories = {"electronics", "apple"};
        Exchange exchange = createExchangeWithBody("Test body for iPod.");
        exchange.getIn().setHeader("solr.field.id", "MA147LL/A");
        exchange.getIn().setHeader("solr.field.cat", categories);
        template.send(exchange);

        // Check things were indexed.
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("id:MA147LL/A");
        QueryResponse response = solrServer.query(solrQuery);

        assertEquals(0, response.getStatus());
        assertEquals(1, response.getResults().getNumFound());

        SolrDocument doc = response.getResults().get(0);
        assertArrayEquals(categories, ((ArrayList)doc.getFieldValue("cat")).toArray());
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        // Set appropriate paths for Solr to use.
        System.setProperty("solr.solr.home", "src/test/resources/solr");
        System.setProperty("solr.data.dir", "target/test-classes/solr/data");

        // Instruct Solr to keep the index in memory, for faster testing.
        System.setProperty("solr.directoryFactory", "solr.RAMDirectoryFactory");

        // Start a Solr instance.
        solrRunner = new JettySolrRunner("/solr", 8999);
        solrRunner.start();

        solrServer = new CommonsHttpSolrServer("http://localhost:8999/solr");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        solrRunner.stop();
    }


    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("solr://localhost:8999/solr");
            }
        };
    }

    @Before
    public void clearIndex() throws Exception {
        // Clear the Solr index.
        solrServer.deleteByQuery("*:*");
        solrServer.commit();
    }
}
