package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.PointDTO;
import catholic.ac.kr.secureuserapp.model.entity.Point;

public class PointMapper {
    public static PointDTO toPointDTO(Point point) {
        PointDTO pointDTO = new PointDTO();

        pointDTO.setUsername(point.getUser().getUsername());
        pointDTO.setUserFullName(point.getUser().getFullName());
        pointDTO.setPoint(point.getPoint());
        pointDTO.setUpdatedAt(point.getUpdatedAt());

        return pointDTO;
    }
}
