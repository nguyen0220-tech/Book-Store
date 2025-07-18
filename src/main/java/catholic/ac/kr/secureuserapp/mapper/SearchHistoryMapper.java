package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.SearchHistoryDTO;
import catholic.ac.kr.secureuserapp.model.entity.SearchHistory;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryMapper {
    public static SearchHistoryDTO toSearchHistoryDTO(SearchHistory searchHistory) {
        SearchHistoryDTO searchHistoryDTO = new SearchHistoryDTO();
        searchHistoryDTO.setId(searchHistory.getId());
        searchHistoryDTO.setSearch(searchHistory.getSearch());
        searchHistoryDTO.setSearchAt(searchHistory.getSearchAt());
        searchHistoryDTO.setType(searchHistory.getType());

        return searchHistoryDTO;
    }

    public static List<SearchHistoryDTO> toSearchHistoryDTOList(List<SearchHistory> searchHistoryList) {
        if (searchHistoryList == null) {
            return null;
        }

        List<SearchHistoryDTO> searchHistoryDTOList = new ArrayList<>();
        for (SearchHistory searchHistory : searchHistoryList) {
            searchHistoryDTOList.add(toSearchHistoryDTO(searchHistory));
        }
        return searchHistoryDTOList;
    }
}
