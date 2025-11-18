package com.brideside.backend.converter;

import com.brideside.backend.dto.TaggedPerson;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA AttributeConverter for converting List<TaggedPerson> to/from JSON string
 * for MySQL JSON column storage.
 */
@Converter
public class JsonListTaggedPersonConverter implements AttributeConverter<List<TaggedPerson>, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonListTaggedPersonConverter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(List<TaggedPerson> attribute) {
        try {
            if (attribute == null || attribute.isEmpty()) {
                return null;
            }
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            logger.error("Error converting List<TaggedPerson> to JSON", e);
            throw new RuntimeException("Error converting tagged people list to JSON", e);
        }
    }
    
    @Override
    public List<TaggedPerson> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty() || dbData.trim().equals("null")) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(dbData, new TypeReference<List<TaggedPerson>>() {});
        } catch (Exception e) {
            logger.error("Error converting JSON to List<TaggedPerson>: {}", dbData, e);
            // Return empty list instead of throwing to prevent breaking existing data
            return new ArrayList<>();
        }
    }
}

