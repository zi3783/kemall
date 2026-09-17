package com.kemall.trade;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-17T17:25:14+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toTarget(User source) {
        if ( source == null ) {
            return null;
        }

        UserDTO userDTO = new UserDTO();

        userDTO.setId( source.getId() );
        userDTO.setUsername( source.getUsername() );
        userDTO.setAge( source.getAge() );

        return userDTO;
    }

    @Override
    public User toSource(UserDTO target) {
        if ( target == null ) {
            return null;
        }

        User user = new User();

        user.setId( target.getId() );
        user.setUsername( target.getUsername() );
        user.setAge( target.getAge() );

        return user;
    }

    @Override
    public List<UserDTO> toTargetList(List<User> sources) {
        if ( sources == null ) {
            return null;
        }

        List<UserDTO> list = new ArrayList<UserDTO>( sources.size() );
        for ( User user : sources ) {
            list.add( toTarget( user ) );
        }

        return list;
    }

    @Override
    public List<User> toSourceList(List<UserDTO> targets) {
        if ( targets == null ) {
            return null;
        }

        List<User> list = new ArrayList<User>( targets.size() );
        for ( UserDTO userDTO : targets ) {
            list.add( toSource( userDTO ) );
        }

        return list;
    }
}
