package com.finflow.ai.module.user;

import com.finflow.ai.module.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerName", expression = "java(user.getManager() != null ? user.getManager().getFirstName() + \" \" + user.getManager().getLastName() : null)")
    UserResponse toResponse(User user);
}
