package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;

@Getter @Setter
public class PointDTO {
    private BigDecimal point;
    private Timestamp updatedAt;
}
