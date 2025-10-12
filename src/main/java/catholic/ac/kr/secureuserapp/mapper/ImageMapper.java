package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.ImageDTO;
import catholic.ac.kr.secureuserapp.model.entity.Image;

public class ImageMapper {
    public static ImageDTO convertToDTO(Image image) {
        ImageDTO imageDTO = new ImageDTO();

        imageDTO.setId(image.getId());
        imageDTO.setImageUrl(image.getImageUrl());
        imageDTO.setUploadAt(image.getUploadAt());

        return imageDTO;
    }
}
