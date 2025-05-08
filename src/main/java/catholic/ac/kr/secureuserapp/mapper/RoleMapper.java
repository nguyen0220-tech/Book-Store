package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.RoleDTO;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper(componentModel = "spring") //Spring quản lý bean
public interface RoleMapper {

    RoleDTO toDTO(Role role);

    Role toRole(RoleDTO roleDTO);

//    Set
    Set<RoleDTO> toDTO(Set<Role> roles);
    Set<Role> toRole(Set<RoleDTO> roleDTOs);
}
