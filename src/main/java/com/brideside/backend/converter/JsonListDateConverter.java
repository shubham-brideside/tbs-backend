package com.brideside.backend.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA AttributeConverter for converting List<LocalDate> to/from JSON string
 * for MySQL JSON column storage.
 */
@Converter
public class JsonListDateConverter implements AttributeConverter<List<LocalDate>, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonListDateConverter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public String convertToDatabaseColumn(List<LocalDate> attribute) {
        try {
            if (attribute == null || attribute.isEmpty()) {
                return null;
            }
            // Convert LocalDate to string list for JSON storage
            List<String> dateStrings = attribute.stream()
                    .map(date -> date.format(DATE_FORMATTER))
                    .collect(Collectors.toList());
            return objectMapper.writeValueAsString(dateStrings);
        } catch (Exception e) {
            logger.error("Error converting List<LocalDate> to JSON", e);
            throw new RuntimeException("Error converting date list to JSON", e);
        }
    }
    
    @Override
    public List<LocalDate> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty() || dbData.trim().equals("null")) {
                return new ArrayList<>();
            }
            // Read as string list first, then convert to LocalDate
            List<String> dateStrings = objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
            return dateStrings.stream()
                    .map(dateStr -> LocalDate.parse(dateStr, DATE_FORMATTER))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error converting JSON to List<LocalDate>: {}", dbData, e);
            // Return empty list instead of throwing to prevent breaking existing data
            return new ArrayList<>();
        }
    }
}

