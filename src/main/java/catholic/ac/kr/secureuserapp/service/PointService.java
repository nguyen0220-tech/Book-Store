package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.PointMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.PointDTO;
import catholic.ac.kr.secureuserapp.model.dto.PointHistory;
import catholic.ac.kr.secureuserapp.model.entity.Point;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.PointRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<PointDTO>> getAllPoints(int page,int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<PointDTO> dtoPage = pointRepository.findAllPoint(pageable);

        return ApiResponse.success("success", dtoPage);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PointDTO> getPointByUsername(String username) {
        Point point = pointRepository.findPointByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("Point not found"));
        PointDTO dto = PointMapper.toPointDTO(point);

        return ApiResponse.success("success", dto);
    }

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

    public ApiResponse<Page<PointHistory>> getPointHistoryByUserId(Long userId,int page,int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<PointHistory> dtoPage = pointRepository.findPointHistoryByUserId(userId,pageable);

        return ApiResponse.success("success", dtoPage);
    }
}
