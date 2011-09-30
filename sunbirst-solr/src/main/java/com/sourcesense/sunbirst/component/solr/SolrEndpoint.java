package com.sourcesense.sunbirst.component.solr;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

/**
 *
 */
public class SolrEndpoint extends DefaultEndpoint
{
    private String address;

    public SolrEndpoint() {}

    public SolrEndpoint(String endpointUri, SolrComponent component, String address) {
        super(endpointUri, component);
        this.address = address;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new SolrProducer(this, address);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
