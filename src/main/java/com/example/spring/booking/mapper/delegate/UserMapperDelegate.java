package com.example.spring.booking.mapper.delegate;

import com.example.spring.booking.entity.User;
import com.example.spring.booking.mapper.UserMapper;
import com.example.spring.booking.web.model.user.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class UserMapperDelegate implements UserMapper {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User requestToUser(UserRequest request) {
        User user = userMapper.requestToUser(request);

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return user;
    }

    @Override
    public User requestToUser(Long userId, UserRequest request) {
        User user = userMapper.requestToUser(userId, request);

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return user;
    }
}
