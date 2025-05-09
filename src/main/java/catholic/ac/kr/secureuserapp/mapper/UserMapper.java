package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.UserDTO;
import catholic.ac.kr.secureuserapp.model.entity.User;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
// để Spring quản lý bean & giúp MapStruct biết dùng RoleMapper để map Set<Role> ↔ Set<RoleDTO>.
public interface UserMapper {
    //    entity -> DTO
    UserDTO toDTO(User user);

    //    DTO -> user
    User toUser(UserDTO userDTO);

    //    List
    List<UserDTO> toDTO(List<User> users);

    List<User> toUser(List<UserDTO> userDTOs);

    //   Custom method xử lý Page
    default Page<UserDTO> toDTO(Page<User> users) {
        List<UserDTO> dtoList = toDTO(users.getContent()); //trả về List<User>, dùng toDTO(List<User>) đã có để ánh xạ.
        return new PageImpl<>(dtoList); //tạo lại Page<UserDTO> với đầy đủ phân trang gốc

    }
}
