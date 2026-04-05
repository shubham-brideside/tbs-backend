package com.brideside.backend.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Trims string values after JSON parse so validation runs on normalized input.
 */
public class TrimStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String v = p.getValueAsString();
        if (v == null) {
            return null;
        }
        return v.trim();
    }
}
