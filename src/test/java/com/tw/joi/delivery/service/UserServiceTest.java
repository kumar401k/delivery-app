package com.tw.joi.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tw.joi.delivery.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private List<User> users;

    @InjectMocks
    private UserService userService;

    @Test
    void fetchUserById_shouldReturnUser_whenUserExists() {
        User mockUser = User.builder().userId("user202").firstName("Test").lastName("Test").build();
        List<User> userList = new ArrayList<>();
        userList.add(mockUser);
        userService = new UserService(userList);

        User result = userService.fetchUserById("user202");

        assertNotNull(result);
        assertEquals("user202", result.getUserId());
    }

    @Test
    void fetchUserById_shouldReturnCorrectUserDetails_whenUserExists() {
        User mockUser = User.builder()
            .userId("user101")
            .firstName("John")
            .lastName("Doe")
            .email("John.Doe@gmail.com")
            .build();
        when(users.stream()).thenReturn(Stream.of(mockUser));

        User result = userService.fetchUserById("user101");

        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("John.Doe@gmail.com", result.getEmail());
        verify(users).stream();
    }

    @Test
    void fetchUserById_shouldReturnNull_whenUserDoesNotExist() {
        User mockUser = User.builder().userId("user101").build();
        when(users.stream()).thenReturn(Stream.of(mockUser));

        User result = userService.fetchUserById("user999");

        assertNull(result);
        verify(users).stream();
    }

    @Test
    void fetchUserById_shouldReturnNull_whenUserListIsEmpty() {
        when(users.stream()).thenReturn(Stream.empty());

        User result = userService.fetchUserById("user101");

        assertNull(result);
        verify(users).stream();
    }

    @Test
    void fetchUserById_shouldReturnNull_whenUserIdIsEmpty() {
        User mockUser = User.builder().userId("user101").build();
        when(users.stream()).thenReturn(Stream.of(mockUser));

        User result = userService.fetchUserById("");

        assertNull(result);
        verify(users).stream();
    }

    @Test
    void fetchUserById_shouldThrowNullPointerException_whenUserIdIsNull() {
        User mockUser = User.builder().userId("user101").build();
        when(users.stream()).thenReturn(Stream.of(mockUser));

        assertThrows(NullPointerException.class, () -> userService.fetchUserById(null));
        verify(users).stream();
    }
}
