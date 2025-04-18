package com.guide.upc.backend.mappers;

import com.guide.upc.backend.dtos.SignUpDto;
import com.guide.upc.backend.dtos.UserDto;
import com.guide.upc.backend.entities.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-17T13:25:21-0500",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.42.0.z20250331-1358, environment: Java 21.0.6 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toUserDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder();

        userDto.apellido( user.getApellido() );
        userDto.foto( user.getFoto() );
        userDto.id( user.getId() );
        userDto.login( user.getLogin() );
        userDto.nombre( user.getNombre() );

        return userDto.build();
    }

    @Override
    public User signUpToUser(SignUpDto signUpDto) {
        if ( signUpDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.apellido( signUpDto.getApellido() );
        user.foto( signUpDto.getFoto() );
        user.id( signUpDto.getId() );
        user.login( signUpDto.getLogin() );
        user.nombre( signUpDto.getNombre() );

        return user.build();
    }
}
