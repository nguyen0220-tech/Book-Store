package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.PostEmotionDTO;
import catholic.ac.kr.secureuserapp.model.entity.PostEmotion;

import java.util.ArrayList;
import java.util.List;

public class PostEmotionMapper {
    public static PostEmotionDTO toPostEmotionDTO(PostEmotion postEmotion) {
        PostEmotionDTO postEmotionDTO = new PostEmotionDTO();

        postEmotionDTO.setId(postEmotion.getId());
        postEmotionDTO.setUserId(postEmotion.getUser().getId());
        postEmotionDTO.setUserName(postEmotion.getUser().getUsername());
        postEmotionDTO.setPostId(postEmotion.getPost().getId());
        postEmotionDTO.setEmotionStatus(String.valueOf(postEmotion.getEmotionStatus()));

        return postEmotionDTO;
    }

    public static List<PostEmotionDTO> postEmotionDTOList(List<PostEmotion> postEmotions) {
        List<PostEmotionDTO> postEmotionDTOList = new ArrayList<>();

        for (PostEmotion postEmotion : postEmotions) {
            postEmotionDTOList.add(toPostEmotionDTO(postEmotion));
        }

        return postEmotionDTOList;
    }
}
