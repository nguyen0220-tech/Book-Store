package catholic.ac.kr.secureuserapp.convert;

import catholic.ac.kr.secureuserapp.Status.Rating;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RatingConverter implements AttributeConverter<Rating, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Rating rating) {
        return rating != null ? rating.getValue() : null;
    }

    @Override
    public Rating convertToEntityAttribute(Integer dbData) {
        return dbData != null ? Rating.getRating(dbData) : null;
    }
}
