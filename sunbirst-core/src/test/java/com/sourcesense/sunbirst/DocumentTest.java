package com.sourcesense.sunbirst;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Wewt!
 */
public class DocumentTest
{
    private Document document;

    @Test
    public void newDocumentShouldBeEmpty() throws Exception {
        document = new Document();
        assertTrue(document.isEmpty());
    }

    @Test
    public void addFieldAndValueToDocument() throws Exception {
        document = new Document();
        document.setFieldValue("id", "doc1");

        assertFalse(document.isEmpty());
        assertEquals("doc1", document.getValue("id"));
    }
}
