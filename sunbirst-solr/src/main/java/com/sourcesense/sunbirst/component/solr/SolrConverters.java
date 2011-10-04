package com.sourcesense.sunbirst.component.solr;

import org.apache.camel.Converter;
import org.apache.camel.Message;
import org.apache.solr.common.SolrInputDocument;

/**
 * Solr specific converters.
 */
@Converter
public final class SolrConverters
{
    private SolrConverters() {
        // Utility class used by Camel.
    }

    /**
     * Converts the given Document Message into a SolrInputDocument.
     *
     * @param message The Camel Message with an embedded Document.
     * @return A SolrInputDocument, ready to be indexed to Solr.
     */
    @Converter
    public static SolrInputDocument toSolrInputDocument(Message message) {
        //return null;
        return new SolrInputDocument();
    }
}
