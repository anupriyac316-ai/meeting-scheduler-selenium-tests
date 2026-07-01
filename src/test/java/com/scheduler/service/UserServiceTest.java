package com.scheduler.service;

import com.scheduler.model.User;
import com.scheduler.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_encodesPasswordAndSavesUser() {
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register("Raj", "raj@example.com", "plainPassword");

        assertEquals("Raj", result.getName());
        assertEquals("raj@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_callsRepositoryDelete() {
        User user = new User();
        user.setId(1L);

        userService.deleteUser(user);

        verify(userRepository).delete(user);
    }

    @Test
    void loadUserByUsername_throwsWhenNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(java.util.Optional.empty());

        assertThrows(
                org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("missing@example.com")
        );
    }
}