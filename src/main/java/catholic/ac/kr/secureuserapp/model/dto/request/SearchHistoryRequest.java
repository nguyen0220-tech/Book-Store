package catholic.ac.kr.secureuserapp.model.dto.request;

import catholic.ac.kr.secureuserapp.Status.SearchType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SearchHistoryRequest {
    private String keyword;
    private SearchType type;
}
