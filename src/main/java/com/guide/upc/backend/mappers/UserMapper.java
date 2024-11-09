package com.guide.upc.backend.mappers;

import com.guide.upc.backend.dtos.SignUpDto;
import com.guide.upc.backend.dtos.UserDto;
import com.guide.upc.backend.entities.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    @Mapping(target = "contraseña", ignore = true)
    User signUpToUser(SignUpDto signUpDto);

}
