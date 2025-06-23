package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.RefreshTokenDTO;
import catholic.ac.kr.secureuserapp.model.entity.RefreshToken;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {
    List<RefreshTokenDTO> toDTO(List<RefreshToken> refreshTokens);
}
