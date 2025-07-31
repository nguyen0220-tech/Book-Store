package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.PostDTO;
import catholic.ac.kr.secureuserapp.model.entity.Post;

public class PostMapper {
    public static PostDTO toPostDTO(Post post) {
        PostDTO postDTO = new PostDTO();

        postDTO.setId(post.getId());
        postDTO.setUsername(post.getUser().getUsername());
        postDTO.setContent(post.getContent());
        postDTO.setPostShare(String.valueOf(post.getPostShare()));
        postDTO.setPostDate(post.getPostDate());
        postDTO.setImageUrl(post.getImageUrl());

        return postDTO;
    }

}
