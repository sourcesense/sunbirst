package com.sourcesense.sunbirst.component.solr;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SolrComponent extends DefaultComponent
{
    private final static Logger log = LoggerFactory.getLogger(SolrComponent.class);

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        String address = "http://" + remaining;

        SolrEndpoint endpoint = new SolrEndpoint(uri, this, address);

        return endpoint;
    }
}
