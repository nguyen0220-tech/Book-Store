package catholic.ac.kr.secureuserapp.model.dto;

import catholic.ac.kr.secureuserapp.Status.SearchType;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class SearchHistoryDTO {
    private Long id;
    private String search;
    private Timestamp searchAt;
    private SearchType type;
}
