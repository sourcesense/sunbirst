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

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;

import org.apache.camel.component.file.FileComponent;
import org.apache.camel.impl.DefaultExchange;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.AfterClass;
import org.junit.Before;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@ContextConfiguration (locations = {"/SolrSpringTest-context.xml"})
public class SolrSpringTest extends AbstractJUnit4SpringContextTests
{
    private static JettySolrRunner solrRunner;
    private static CommonsHttpSolrServer solrServer;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @DirtiesContext
    @Test
    public void endToEndSpringContext() throws Exception {
        template.sendBody(new File("src/test/resources/data/books.xml"));

        // TODO: Fix race condition.
        Thread.sleep(5000);

        // Check things were indexed.
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");
        QueryResponse response = solrServer.query(solrQuery);

        assertEquals(0, response.getStatus());
        assertEquals(4, response.getResults().getNumFound());

        // Check fields were indexed correctly.
        solrQuery.setQuery("id:Learning XML");
        response = solrServer.query(solrQuery);

        SolrDocument doc = response.getResults().get(0);
        assertEquals(Arrays.asList("Learning XML"), doc.getFieldValue("title"));
        assertEquals(Arrays.asList("Web", "Technology", "Computers"), doc.getFieldValue("cat"));
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        // Set appropriate paths for Solr to use.
        System.setProperty("solr.solr.home", "src/test/resources/solr");
        System.setProperty("solr.data.dir", "target/test-classes/solr/data");

        // Instruct Solr to keep the index in memory, for faster testing.
        System.setProperty("solr.directoryFactory", "solr.RAMDirectoryFactory");

        // Start a Solr instance.
        solrRunner = new JettySolrRunner("/solr", 8983);
        solrRunner.start();

        solrServer = new CommonsHttpSolrServer("http://localhost:8983/solr");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        solrRunner.stop();
    }

    @Before
    public void clearIndex() throws Exception {
        // Clear the Solr index.
        solrServer.deleteByQuery("*:*");
        solrServer.commit();
    }
}
