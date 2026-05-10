package com.rayaan.ailearn.model.converter;

import com.rayaan.ailearn.model.enums.MasteryStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MasteryStatusConverter implements AttributeConverter<MasteryStatus, String> {

    @Override
    public String convertToDatabaseColumn(MasteryStatus attribute) {
        if (attribute == null) {
            return null;
        }

        return switch (attribute) {
            case NEW -> "N";
            case IN_PROGRESS -> "IP";
            case MASTERED -> "M";
        };
    }

    @Override
    public MasteryStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        return switch (dbData.trim().toUpperCase()) {
            case "N", "NEW" -> MasteryStatus.NEW;
            case "IP", "IN_PROGRESS" -> MasteryStatus.IN_PROGRESS;
            case "M", "MASTERED" -> MasteryStatus.MASTERED;
            default -> MasteryStatus.NEW;
        };
    }
}