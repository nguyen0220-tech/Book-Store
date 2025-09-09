package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.mapper.PointMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.PointDTO;
import catholic.ac.kr.secureuserapp.model.entity.Point;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.PointRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    public ApiResponse<PointDTO> getPointByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        Point point = pointRepository.findByUserId(userId)
                .orElseGet(() -> {
                            Point p = Point.builder()
                                    .user(user)
                                    .point(BigDecimal.ZERO)
                                    .updatedAt(new Timestamp(System.currentTimeMillis()))
                                    .build();

                            return pointRepository.save(p);
                        }
                );

        PointDTO pointDTO = PointMapper.toPointDTO(point);

        return ApiResponse.success("Point found", pointDTO);
    }
}
