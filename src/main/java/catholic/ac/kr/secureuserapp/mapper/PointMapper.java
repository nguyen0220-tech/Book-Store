package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.PointDTO;
import catholic.ac.kr.secureuserapp.model.entity.Point;

public class PointMapper {
    public static PointDTO toPointDTO(Point point) {
        PointDTO pointDTO = new PointDTO();

        pointDTO.setPoint(point.getPoint());
        pointDTO.setUpdatedAt(point.getUpdatedAt());

        return pointDTO;
    }
}
