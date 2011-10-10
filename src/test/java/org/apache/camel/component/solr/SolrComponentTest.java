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

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import org.apache.solr.common.SolrException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SolrComponentTest extends CamelTestSupport {

    private static JettySolrRunner solrRunner;
    private static CommonsHttpSolrServer solrServer;

    @EndpointInject(uri = "solr://localhost:8999/solr")
    protected SolrEndpoint solrEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Test
    public void indexSingleDocumentOnlyWithId() throws Exception {
        Exchange exchange = createExchangeWithBody(null);
        exchange.getIn().setHeader("solr.field.id", "MA147LL/A");

        template.send(exchange);
        solrCommit();

        // Check things were indexed.
        QueryResponse response = executeSolrQuery("id:MA147LL/A");

        assertEquals(0, response.getStatus());
        assertEquals(1, response.getResults().getNumFound());
    }

    @Test
    public void caughtSolrExceptionIsHandledElegantly() throws Exception {
        Exchange exchange = createExchangeWithBody(null);
        exchange.getIn().setHeader("solr.field.name", "Missing required field throws exception.");

        template.send(exchange);

        assertEquals(SolrException.class, exchange.getException().getClass());
    }

    @Test
    public void setHeadersAsSolrFields() throws Exception {
        Exchange exchange = createExchangeWithBody("Body is ignored");
        exchange.getIn().setHeader("solr.field.id", "MA147LL/A");
        exchange.getIn().setHeader("solr.field.name", "Apple 60 GB iPod with Video Playback Black");
        exchange.getIn().setHeader("solr.field.manu", "Apple Computer Inc.");

        template.send(exchange);
        solrCommit();

        QueryResponse response = executeSolrQuery("id:MA147LL/A");

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
        solrCommit();

        // Check things were indexed.
        QueryResponse response = executeSolrQuery("id:MA147LL/A");

        assertEquals(0, response.getStatus());
        assertEquals(1, response.getResults().getNumFound());

        SolrDocument doc = response.getResults().get(0);
        assertArrayEquals(categories, ((List) doc.getFieldValue("cat")).toArray());
    }

    @Test
    public void indexDocumentsAndThenCommit() throws Exception {
        Exchange exchange = createExchangeWithBody(null);
        exchange.getIn().setHeader("solr.field.id", "MA147LL/A");
        exchange.getIn().setHeader("solr.field.name", "Apple 60 GB iPod with Video Playback Black");
        exchange.getIn().setHeader("solr.field.manu", "Apple Computer Inc.");
        template.send(exchange);

        QueryResponse response = executeSolrQuery("*:*");
        assertEquals(0, response.getStatus());
        assertEquals(0, response.getResults().getNumFound());

        solrCommit();

        QueryResponse afterCommitResponse = executeSolrQuery("*:*");
        assertEquals(0, afterCommitResponse.getStatus());
        assertEquals(1, afterCommitResponse.getResults().getNumFound());
    }

    @Test
    public void invalidSolrParametersAreIgnored() throws Exception {
        Exchange exchange = createExchangeWithBody(null);
        exchange.getIn().setHeader("solr.field.id", "MA147LL/A");
        exchange.getIn().setHeader("solr.field.name", "Apple 60 GB iPod with Video Playback Black");
        exchange.getIn().setHeader("solr.param.invalid-param", "this is ignored");

        template.send(exchange);
        solrCommit();

        QueryResponse response = executeSolrQuery("*:*");
        assertEquals(0, response.getStatus());
        assertEquals(1, response.getResults().getNumFound());
    }

    @Test
    public void indexDocumentsToCSVUpdateHandlerWithoutParameters() throws Exception {
        solrEndpoint.setRequestHandler("/update/csv");
        solrEndpoint.setSendFile(true);

        Exchange exchange = createExchangeWithBody(new File("src/test/resources/data/books.csv"));
        template.send(exchange);
        solrCommit();

        QueryResponse response = executeSolrQuery("*:*");
        assertEquals(0, response.getStatus());
        assertEquals(10, response.getResults().getNumFound());

        response = executeSolrQuery("id:0553573403");
        SolrDocument doc = response.getResults().get(0);
        assertEquals("A Game of Thrones", doc.getFieldValue("name"));
        assertEquals(7.99f, doc.getFieldValue("price"));
    }

    @Test
    public void indexDocumentsToCSVUpdateHandlerWithParameters() throws Exception {
        solrEndpoint.setRequestHandler("/update/csv");
        solrEndpoint.setSendFile(true);

        Exchange exchange = createExchangeWithBody(new File("src/test/resources/data/books.csv"));
        exchange.getIn().setHeader("solr.param.fieldnames", "id,cat,name,price,inStock,author_t,series_t,sequence_i,genre_s");
        exchange.getIn().setHeader("solr.param.skip", "cat,sequence_i,genre_s");
        exchange.getIn().setHeader("solr.param.skipLines", 1);

        template.send(exchange);
        solrCommit();

        QueryResponse response = executeSolrQuery("*:*");
        assertEquals(0, response.getStatus());
        assertEquals(10, response.getResults().getNumFound());

        SolrDocument doc = response.getResults().get(0);
        assertFalse(doc.getFieldNames().contains("cat"));
    }

    @Test
    public void indexPDFDocumentToExtractingRequestHandler() throws Exception {
        solrEndpoint.setRequestHandler("/update/extract");
        solrEndpoint.setSendFile(true);

        Exchange exchange = createExchangeWithBody(new File("src/test/resources/data/tutorial.pdf"));
        exchange.getIn().setHeader("solr.param.literal.id", "tutorial.pdf");

        template.send(exchange);
        solrCommit();

        QueryResponse response = executeSolrQuery("*:*");
        assertEquals(0, response.getStatus());
        assertEquals(1, response.getResults().getNumFound());

        SolrDocument doc = response.getResults().get(0);
        assertEquals("Solr", doc.getFieldValue("subject"));
        assertEquals("tutorial.pdf", doc.getFieldValue("id"));
        assertEquals(Arrays.asList("application/pdf"), doc.getFieldValue("content_type"));
    }

    @Test
    @Ignore("No real advantage has yet been discovered to specifying the file in a header.")
    public void indexPDFDocumentSpecifyingFileInParameters() throws Exception {
        solrEndpoint.setRequestHandler("/update/extract");
        solrEndpoint.setSendFile(true);

        Exchange exchange = createExchangeWithBody(null);
        exchange.getIn().setHeader("solr.param.stream.file", "src/test/resources/data/tutorial.pdf");
        exchange.getIn().setHeader("solr.param.literal.id", "tutorial.pdf");

        template.send(exchange);
        solrCommit();

        QueryResponse response = executeSolrQuery("*:*");
        assertEquals(0, response.getStatus());
        assertEquals(1, response.getResults().getNumFound());

        SolrDocument doc = response.getResults().get(0);
        assertEquals("Solr", doc.getFieldValue("subject"));
        assertEquals("tutorial.pdf", doc.getFieldValue("id"));
        assertEquals(Arrays.asList("application/pdf"), doc.getFieldValue("content_type"));
    }

    private void solrCommit() {
        Exchange commitExchange = createExchangeWithBody(null);
        commitExchange.getIn().setHeader(SolrHeaders.OPERATION, SolrHeaders.COMMIT);
        template.send(commitExchange);
    }

    private QueryResponse executeSolrQuery(String query) throws SolrServerException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query);
        return solrServer.query(solrQuery);
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
