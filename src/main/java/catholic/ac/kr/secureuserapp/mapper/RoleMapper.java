package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.RoleDTO;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring") //Spring quản lý bean
public interface RoleMapper {

    RoleDTO toDTO(Role role);

    Role toRole(RoleDTO roleDTO);

//    Set
    Set<RoleDTO> toDTO(Set<Role> roles);
    Set<Role> toRole(Set<RoleDTO> roleDTOs);

    //    list
    List<RoleDTO> toRole(List<Role> roles);
}
