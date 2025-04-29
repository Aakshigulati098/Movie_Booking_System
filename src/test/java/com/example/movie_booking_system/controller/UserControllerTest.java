package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.dto.UserDTO;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUser_ShouldReturnUser() {
        String email = "test@example.com";
        Users user = new Users();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(user);

        Users result = userController.getUser(email);

        assertEquals(user, result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void getUserProfile_ShouldReturnUser() {
        String email = "test@example.com";
        Users user = new Users();
        user.setEmail(email);

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(user);

        Users result = userController.getUserProfile(authentication);

        assertEquals(user, result);
        verify(authentication, times(1)).getName();
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void updateUserProfile_ShouldUpdateAndReturnSuccessMessage() {
        String email = "test@example.com";
        UserDTO updatedUser = new UserDTO();
        updatedUser.setName("Updated Name");
        updatedUser.setPhone("1234567890");

        Users existingUser = new Users();
        existingUser.setEmail(email);

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(existingUser);

        String result = userController.updateUserProfile(updatedUser, authentication);

        assertEquals("Profile updated successfully", result);
        assertEquals("Updated Name", existingUser.getName());
        assertEquals("1234567890", existingUser.getPhone());
        verify(authentication, times(1)).getName();
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUserProfile_ShouldReturnUserNotFoundMessage() {
        String email = "test@example.com";
        UserDTO updatedUser = new UserDTO();

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(null);

        String result = userController.updateUserProfile(updatedUser, authentication);

        assertEquals("User not found", result);
        verify(authentication, times(1)).getName();
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any());
    }
}