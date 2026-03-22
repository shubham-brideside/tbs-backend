package com.brideside.backend.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Accepts JSON strings (e.g. "100-300") or numbers (e.g. 250) for fields stored as text.
 */
public class StringOrNumberAsStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        if (p.currentToken().isNumeric()) {
            return p.getValueAsString();
        }
        return p.getValueAsString();
    }
}
