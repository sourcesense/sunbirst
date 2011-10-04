package com.sourcesense.sunbirst.component.solr;

import com.sourcesense.sunbirst.Document;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.common.SolrInputDocument;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * Testing the conversion of internal Documents to SolrInputDocuments.
 */
public class SolrConvertersTest extends CamelTestSupport
{
    private static JettySolrRunner solrRunner;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Test
    public void documentMessageToSolrInputDocument() throws Exception {
        SolrInputDocument expected = null;

        Document document = new Document();
        document.setFieldValue("id", "MA147LL/A");
        document.setFieldValue("name", "Apple 60 GB iPod with Video Playback Black");
        document.setFieldValue("manu", "Apple Computer Inc.");
        document.setFieldValue("cat", "electronics");

        resultEndpoint.expectedBodiesReceived(expected);

        template.sendBody(document);

        resultEndpoint.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("solr://localhost:8983/solr").to("mock:result");
            }
        };
    }

    @Override
    protected void doPreSetup() throws Exception {
        // Start a Solr instance.
        solrRunner = new JettySolrRunner("/solr", 8983);
        solrRunner.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        solrRunner.stop();
    }
}
