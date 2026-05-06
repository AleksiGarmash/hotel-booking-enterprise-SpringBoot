package com.example.spring.booking.service;

import com.example.spring.booking.entity.Role;
import com.example.spring.booking.entity.User;
import com.example.spring.booking.exception.InvalidDataException;
import com.example.spring.booking.exception.ResourceNotFoundException;
import com.example.spring.booking.mapper.UserMapper;
import com.example.spring.booking.repository.UserRepository;
import com.example.spring.booking.statistic.event.UserRegistrationEvent;
import com.example.spring.booking.statistic.producer.KafkaEventPublisher;
import com.example.spring.booking.web.model.user.UserRequest;
import com.example.spring.booking.web.model.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private KafkaEventPublisher kafkaEventPublisher;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).username("testuser")
                .password("encoded_pass").email("test@mail.com")
                .role(Role.ROLE_USER).build();

        request = new UserRequest();
        request.setUsername("testuser");
        request.setPassword("password");
        request.setEmail("test@mail.com");
    }

    // ─── Регистрация ──────────────────────────────────────────────

    @Test
    @DisplayName("create: новый пользователь → сохраняется и Kafka-событие публикуется")
    void create_newUser_savesAndPublishesKafkaEvent() {
        UserResponse response = new UserResponse();

        when(userRepository.existsByUsernameOrEmail("testuser", "test@mail.com")).thenReturn(false);
        when(userMapper.requestToUser(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.userToResponse(user)).thenReturn(response);

        UserResponse result = userService.create(request);

        assertThat(result).isSameAs(response);

        // проверяем что Kafka получила событие с правильными данными
        ArgumentCaptor<UserRegistrationEvent> captor =
                ArgumentCaptor.forClass(UserRegistrationEvent.class);
        verify(kafkaEventPublisher).publishUserRegistration(captor.capture());

        UserRegistrationEvent event = captor.getValue();
        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getUsername()).isEqualTo("testuser");
        assertThat(event.getEmail()).isEqualTo("test@mail.com");
    }

    @Test
    @DisplayName("create: username или email уже существует → InvalidDataException")
    void create_duplicateUsernameOrEmail_throwsInvalidDataException() {
        when(userRepository.existsByUsernameOrEmail("testuser", "test@mail.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("already exist");

        verifyNoInteractions(kafkaEventPublisher);
        verify(userRepository, never()).save(any());
    }

    // ─── loadUserByUsername ───────────────────────────────────────

    @Test
    @DisplayName("loadUserByUsername: существующий пользователь → UserDetails с правильной ролью")
    void loadUserByUsername_exists_returnsUserDetailsWithRole() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        var userDetails = userService.loadUserByUsername("testuser");

        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getAuthorities()).anyMatch(a ->
                a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    @DisplayName("loadUserByUsername: пользователь не найден → UsernameNotFoundException")
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("nobody"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ─── Поиск ───────────────────────────────────────────────────

    @Test
    @DisplayName("findById: существующий ID → возвращает ответ")
    void findById_exists_returnsResponse() {
        UserResponse response = new UserResponse();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.userToResponse(user)).thenReturn(response);

        assertThat(userService.findById(1L)).isSameAs(response);
    }

    @Test
    @DisplayName("findById: несуществующий ID → ResourceNotFoundException")
    void findById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── Удаление ─────────────────────────────────────────────────

    @Test
    @DisplayName("delete: существующий ID → deleteById вызван")
    void delete_exists_deleteCalled() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: несуществующий ID → ResourceNotFoundException")
    void delete_notFound_throwsResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }

    // ─── Обновление ──────────────────────────────────────────────

    @Test
    @DisplayName("update: смена username на занятый → InvalidDataException")
    void update_duplicateUsername_throwsInvalidDataException() {
        request.setUsername("occupied");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("occupied")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("already exist");
    }

    @Test
    @DisplayName("update: смена email на занятый → InvalidDataException")
    void update_duplicateEmail_throwsInvalidDataException() {
        request.setUsername("newusername");
        request.setEmail("occupied@mail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.existsByEmail("occupied@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("already exist");
    }
}