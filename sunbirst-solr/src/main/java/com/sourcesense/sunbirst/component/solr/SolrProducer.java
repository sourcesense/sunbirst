package com.sourcesense.sunbirst.component.solr;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

/**
 * A Producer to send documents to Solr using SolrJ.
 */
public class SolrProducer extends DefaultProducer
{
    private static final Logger Log = LoggerFactory.getLogger(SolrProducer.class);

    private SolrServer solrServer;

    public SolrProducer(Endpoint endpoint, String address) {
        super(endpoint);

        try {
            solrServer = new CommonsHttpSolrServer(address);
        } catch (MalformedURLException muex) {
            log.error("SEVERE: Could not connect to Solr server: {}", address);
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        // TODO.
        if (solrServer == null) throw new Exception();

        // TODO: This doesn't actually convert yet. Need to write a converter.
        SolrInputDocument doc = exchange.getIn().getBody(SolrInputDocument.class);
        solrServer.add(doc);
        solrServer.commit();
    }

    @Override
    // TODO: What does this do?
    public SolrEndpoint getEndpoint() {
        return (SolrEndpoint) super.getEndpoint();
    }
}
