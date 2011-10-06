package org.apache.camel.component.solr;

import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class InitSolrEndpointTest extends CamelTestSupport {

    private String solrUrl = "solr://localhost:8999/solr";

    @Test
    public void testEndpointCreatedWithAllOptions() throws Exception {
        SolrEndpoint solrEndpoint = (SolrEndpoint) context.getEndpoint(solrUrl + getFullOptions());
        assertNotNull(solrEndpoint);
    }

    @Test(expected = ResolveEndpointFailedException.class)
    public void wrongURLFormatFailsEndpointCreation() throws Exception {
        context.getEndpoint("solr://localhost:-99/solr");
    }

    private String getFullOptions() {
        return "?maxRetries=1&soTimeout=100&connectionTimeout=100&defaultMaxConnectionsPerHost=100&maxTotalConnections=100&followRedirects=false&allowCompression=true";
    }
}
