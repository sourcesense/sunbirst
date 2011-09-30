package com.sourcesense.sunbirst;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple internal Document representation based on the OpenPipe Document
 * class.
 *
 * TODO: At the moment this is only used for testing message body conversion and
 * endpoints.
 */
public class Document
{
    private final Map<String, String> fieldMap = new LinkedHashMap<String, String>();

    public Document() {}

    public boolean isEmpty() {
        return fieldMap.isEmpty();
    }

    public void setFieldValue(String fieldName, String value) {
        if (fieldName != null && value != null) {
            fieldMap.put(fieldName, value);
        }
    }

    public String getValue(String fieldName) {
        return (fieldName == null) ? null : fieldMap.get(fieldName);
    }
}
