package catholic.ac.kr.secureuserapp.model.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record PointHistory(
        Timestamp updatedAt,
        BigDecimal pointHoard,
        BigDecimal pointUsage
) {
}
