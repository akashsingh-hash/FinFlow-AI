package com.finflow.ai.module.user;

import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.user.dto.UserResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T09:57:40+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.companyId( userCompanyId( user ) );
        userResponse.companyName( userCompanyName( user ) );
        userResponse.managerId( userManagerId( user ) );
        userResponse.id( user.getId() );
        userResponse.email( user.getEmail() );
        userResponse.firstName( user.getFirstName() );
        userResponse.lastName( user.getLastName() );
        userResponse.role( user.getRole() );
        userResponse.status( user.getStatus() );
        userResponse.department( user.getDepartment() );
        userResponse.createdAt( user.getCreatedAt() );
        userResponse.updatedAt( user.getUpdatedAt() );

        userResponse.managerName( user.getManager() != null ? user.getManager().getFirstName() + " " + user.getManager().getLastName() : null );

        return userResponse.build();
    }

    private Long userCompanyId(User user) {
        if ( user == null ) {
            return null;
        }
        Company company = user.getCompany();
        if ( company == null ) {
            return null;
        }
        Long id = company.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String userCompanyName(User user) {
        if ( user == null ) {
            return null;
        }
        Company company = user.getCompany();
        if ( company == null ) {
            return null;
        }
        String name = company.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long userManagerId(User user) {
        if ( user == null ) {
            return null;
        }
        User manager = user.getManager();
        if ( manager == null ) {
            return null;
        }
        Long id = manager.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
