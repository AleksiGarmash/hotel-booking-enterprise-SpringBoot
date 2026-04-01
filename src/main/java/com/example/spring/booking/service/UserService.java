package com.example.spring.booking.service;

import com.example.spring.booking.entity.User;
import com.example.spring.booking.exception.InvalidDataException;
import com.example.spring.booking.exception.ResourceNotFoundException;
import com.example.spring.booking.mapper.UserMapper;
import com.example.spring.booking.repository.UserRepository;
import com.example.spring.booking.statistic.event.UserRegistrationEvent;
import com.example.spring.booking.statistic.producer.KafkaEventPublisher;
import com.example.spring.booking.web.model.user.UserListResponse;
import com.example.spring.booking.web.model.user.UserRequest;
import com.example.spring.booking.web.model.user.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading User by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(MessageFormat.format("User not found: {}", username)));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name().replace("ROLE_", ""))
                .build();
    }

    public UserListResponse findAll(){
        log.info("Get all Users");
        return userMapper.userListToResponseList(userRepository.findAll());
    }

    public Page<UserResponse> findAll(Pageable pageable) {
        log.info("Get Users with pagination: page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());

        return userRepository.findAll(pageable)
                .map(userMapper::userToResponse);
    }

    public UserResponse findById(Long id) {
        log.info("Get User by ID: {}", id);

        return userMapper.userToResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("User with ID {} not found!", id))));
    }

    public UserResponse findByUsername(String username) {
        log.info("Get User by username: {}", username);

        return userMapper.userToResponse(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("User with username {} nit found!", username))));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        log.info("Creating new User: {}", request.getUsername());

        if (userRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            throw new InvalidDataException(
                    MessageFormat.format("User with username {} and email {} already exist", request.getUsername(), request.getEmail()));
        }

        User user = userMapper.requestToUser(request);
        User savedUser = userRepository.save(user);
        log.info("User created");

        UserRegistrationEvent event = UserRegistrationEvent.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .build();
        kafkaEventPublisher.publishUserRegistration(event);

        return userMapper.userToResponse(savedUser);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        log.info("Updating User: {}, {}", id, request.getUsername());

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                MessageFormat.format("User with ID {} not found!", id)));

        if (request.getUsername() != null &&
            !request.getUsername().equals(user.getUsername()) &&
            userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidDataException(
                    MessageFormat.format("User with username {} already exist!", request.getUsername()));
        }

        if (request.getEmail() != null &&
            !request.getEmail().equals(user.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidDataException(
                    MessageFormat.format("User with email {} already exist!", request.getEmail()));
        }

        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());

        User updatedUser = userRepository.save(user);
        log.info("User updated");

        return userMapper.userToResponse(updatedUser);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting User with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(MessageFormat.format("User with ID {} not found!", id));
        }

        userRepository.deleteById(id);
        log.info("User deleted");
    }
}
