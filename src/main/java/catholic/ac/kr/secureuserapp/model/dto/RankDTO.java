package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter @Setter
public class RankDTO {
    private String username;
    private String fullUsername;
    private String rank;
    private Timestamp updatedAt;
}
