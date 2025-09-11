package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.RankDTO;
import catholic.ac.kr.secureuserapp.model.entity.Rank;

public class RankMapper {
    public static RankDTO toRankDTO(Rank rank) {
        RankDTO rankDTO = new RankDTO();

        rankDTO.setUsername(rank.getUser().getUsername());
        rankDTO.setFullUsername(rank.getUser().getFullName());
        rankDTO.setRank(rank.getRank().name());
        rankDTO.setUpdatedAt(rank.getUpdatedAt());

        return rankDTO;
    }
}
