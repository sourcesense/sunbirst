package org.apache.camel.component.solr;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;

public class SolrServerMissingTest extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Test
    public void indexSingleDocumentToNonexistentServer() throws Exception {
        Exchange exchange = createExchangeWithBody(null);
        exchange.getIn().setHeader("solr.field.id", "MA147LL/A");

        template.send(exchange);
        assertEquals(SolrServerException.class, exchange.getException().getClass());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("solr://localhost:8999/missingSolr");
            }
        };
    }
}
