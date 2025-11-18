package com.brideside.backend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA AttributeConverter for converting List<String> to/from JSON string
 * for MySQL JSON column storage.
 */
@Converter
public class JsonListStringConverter implements AttributeConverter<List<String>, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonListStringConverter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            if (attribute == null || attribute.isEmpty()) {
                return null;
            }
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            logger.error("Error converting List<String> to JSON", e);
            throw new RuntimeException("Error converting list to JSON", e);
        }
    }
    
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty() || dbData.trim().equals("null")) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            logger.error("Error converting JSON to List<String>: {}", dbData, e);
            // Return empty list instead of throwing to prevent breaking existing data
            return new ArrayList<>();
        }
    }
}

