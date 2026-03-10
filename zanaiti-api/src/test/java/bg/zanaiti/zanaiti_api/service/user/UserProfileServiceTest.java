package bg.zanaiti.zanaiti_api.service.user;

import bg.zanaiti.zanaiti_api.dto.User.UserDto;
import bg.zanaiti.zanaiti_api.dto.User.UserUpdateDto;
import bg.zanaiti.zanaiti_api.model.User;
import bg.zanaiti.zanaiti_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserProfileService profileService;

    private User testUser;
    private final Long userId = 1L;
    private final String currentPassword = "oldPassword123";
    private final String encodedPassword = "encodedOldPassword123";
    private final String newPassword = "newPassword456";
    private final String encodedNewPassword = "encodedNewPassword456";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .password(encodedPassword)
                .totalPoints(100)
                .createdAt(LocalDateTime.now().minusDays(30))
                .isActive(true)
                .roles(new HashSet<>())
                .build();
    }

    // ============= getUserProfile() TESTS =============

    @Test
    void getUserProfile_ShouldReturnUserDto_WhenUserExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserDto result = profileService.getUserProfile(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName());
        assertEquals(100, result.getTotalPoints());
        assertTrue(result.isActive());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserProfile_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.getUserProfile(999L));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
    }

    // ============= updateUserProfile() TESTS =============

    @Test
    void updateUserProfile_ShouldUpdateFullNameOnly_WhenOnlyNameProvided() {
        // Arrange
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .fullName("Updated Name")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = profileService.updateUserProfile(userId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getFullName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(encodedPassword, testUser.getPassword());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateUserProfile_ShouldUpdateEmail_WhenNewEmailProvided() {
        // Arrange
        String newEmail = "newemail@example.com";
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email(newEmail)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = profileService.updateUserProfile(userId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(newEmail, result.getEmail());
        assertEquals("Test User", result.getFullName());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail(newEmail);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        String existingEmail = "existing@example.com";
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email(existingEmail)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.updateUserProfile(userId, updateDto));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail(existingEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_ShouldNotCheckEmailUniqueness_WhenEmailUnchanged() {
        // Arrange
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("test@example.com")
                .fullName("New Name")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = profileService.updateUserProfile(userId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("New Name", result.getFullName());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).existsByEmail(anyString()); // do not check
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_ShouldUpdatePassword_WhenNewPasswordProvided() {
        // Arrange
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .password(newPassword)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = profileService.updateUserProfile(userId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(encodedNewPassword, testUser.getPassword());
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_ShouldUpdateAllFields_WhenAllFieldsProvided() {
        // Arrange
        String newEmail = "newemail@example.com";
        String newName = "New Full Name";
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .fullName(newName)
                .email(newEmail)
                .password(newPassword)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = profileService.updateUserProfile(userId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(newName, result.getFullName());
        assertEquals(newEmail, result.getEmail());
        assertEquals(encodedNewPassword, testUser.getPassword());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail(newEmail);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .fullName("New Name")
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.updateUserProfile(999L, updateDto));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_ShouldIgnoreEmptyPassword_WhenBlankProvided() {
        // Arrange
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .fullName("New Name")
                .password("   ") // whitespace string
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = profileService.updateUserProfile(userId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", result.getFullName());
        assertEquals(encodedPassword, testUser.getPassword());

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_ShouldIgnoreNullPassword_WhenNullProvided() {
        // Arrange
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .fullName("New Name")
                .password(null)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = profileService.updateUserProfile(userId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", result.getFullName());
        assertEquals(encodedPassword, testUser.getPassword());

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }
}