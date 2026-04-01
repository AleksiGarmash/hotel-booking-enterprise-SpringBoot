package com.example.spring.booking.mapper;

import com.example.spring.booking.entity.User;
import com.example.spring.booking.mapper.delegate.UserMapperDelegate;
import com.example.spring.booking.web.model.user.UserListResponse;
import com.example.spring.booking.web.model.user.UserRequest;
import com.example.spring.booking.web.model.user.UserResponse;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@DecoratedWith(UserMapperDelegate.class)
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User requestToUser(UserRequest request);

    @Mapping(source = "userId", target = "id")
    User requestToUser(Long userId, UserRequest request);

    UserResponse userToResponse(User user);

    default UserListResponse userListToResponseList(List<User> users) {
        UserListResponse response = new UserListResponse();

        response.setUsers(users.stream()
                .map(this::userToResponse)
                .collect(Collectors.toList()));

        return response;
    }
}
