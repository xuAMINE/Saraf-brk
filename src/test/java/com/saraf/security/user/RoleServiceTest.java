package com.saraf.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.saraf.security.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class RoleServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = User.builder()
                .id(1)
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    void testUpdateUserRole_Success() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // When
        roleService.updateUserRole(1, Role.ADMIN);

        // Then
        verify(userRepository).save(testUser);
        assertThat(testUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void testUpdateUserRole_UserNotFound() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.updateUserRole(1, Role.ADMIN))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with ID 1 not found");
    }
}
