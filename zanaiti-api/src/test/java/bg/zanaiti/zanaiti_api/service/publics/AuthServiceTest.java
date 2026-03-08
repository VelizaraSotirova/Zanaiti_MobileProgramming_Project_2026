package bg.zanaiti.zanaiti_api.service.publics;

import bg.zanaiti.zanaiti_api.dto.User.UserCreateDto;
import bg.zanaiti.zanaiti_api.dto.User.UserDto;
import bg.zanaiti.zanaiti_api.security.CustomUserDetails;
import bg.zanaiti.zanaiti_api.security.dto.LoginRequest;
import bg.zanaiti.zanaiti_api.security.dto.LoginResponse;
import bg.zanaiti.zanaiti_api.security.jwt.JwtService;
import bg.zanaiti.zanaiti_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails customUserDetails;

    @InjectMocks
    private AuthService authService;

    private UserCreateDto validRegisterDto;
    private UserDto userDto;
    private LoginRequest validLoginRequest;
    private final String testToken = "eyJhbGciOiJIUzI1NiIs...";

    @BeforeEach
    void setUp() {
        // Test data setup
        validRegisterDto = UserCreateDto.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .totalPoints(0)
                .roles(Set.of("USER"))
                .isActive(true)
                .build();

        validLoginRequest = new LoginRequest("testuser", "password123");
    }

    // ============= register() TESTS =============

    @Test
    void register_ShouldReturnLoginResponse_WhenRegistrationIsSuccessful() {
        // Arrange
        when(userService.createUser(any(UserCreateDto.class))).thenReturn(userDto);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(customUserDetails);
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn(testToken);
        //when(customUserDetails.getUser()).thenReturn(any());

        // Act
        LoginResponse response = authService.register(validRegisterDto);

        // Assert
        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertEquals(0, response.getTotalPoints());
        assertEquals("USER", response.getRole());

        verify(userService, times(1)).createUser(validRegisterDto);
        verify(userDetailsService, times(1)).loadUserByUsername("testuser");
        verify(jwtService, times(1)).generateToken(customUserDetails);
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        when(userService.createUser(any(UserCreateDto.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(validRegisterDto));

        assertEquals("Username already exists", exception.getMessage());
        verify(userService, times(1)).createUser(validRegisterDto);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userService.createUser(any(UserCreateDto.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(validRegisterDto));

        assertEquals("Email already exists", exception.getMessage());
        verify(userService, times(1)).createUser(validRegisterDto);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_ShouldThrowException_WhenUserServiceThrowsOtherException() {
        // Arrange
        when(userService.createUser(any(UserCreateDto.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(validRegisterDto));

        assertEquals("Database error", exception.getMessage());
    }

    // ============= login() TESTS =============

    @Test
    void login_ShouldReturnLoginResponse_WhenCredentialsAreValid() {
        // Arrange
        Authentication authToken = new UsernamePasswordAuthenticationToken("testuser", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        //when(customUserDetails.getUser()).thenReturn(null);
        when(jwtService.generateToken(customUserDetails)).thenReturn(testToken);
        when(userService.getUserByUsername("testuser")).thenReturn(userDto);

        // Act
        LoginResponse response = authService.login(validLoginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertEquals(0, response.getTotalPoints());
        assertEquals("USER", response.getRole());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(customUserDetails);
        verify(userService, times(1)).getUserByUsername("testuser");
    }

    @Test
    void login_ShouldThrowException_WhenUsernameNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> authService.login(validLoginRequest));

        assertEquals("User not found", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, never()).generateToken(any());
        verify(userService, never()).getUserByUsername(anyString());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.login(validLoginRequest));

        assertEquals("Bad credentials", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, never()).generateToken(any());
        verify(userService, never()).getUserByUsername(anyString());
    }

    // ============= BORDER TEST CASES =============

    @Test
    void register_WithNullUsername_ShouldThrowException() {
        // Arrange
        UserCreateDto invalidDto = UserCreateDto.builder()
                .username(null)
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        when(userService.createUser(any(UserCreateDto.class)))
                .thenThrow(new RuntimeException("Username is required"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(invalidDto));

        assertNotNull(exception.getMessage());
    }

    @Test
    void register_WithNullEmail_ShouldThrowException() {
        // Arrange
        UserCreateDto invalidDto = UserCreateDto.builder()
                .username("testuser")
                .email(null)
                .password("password123")
                .fullName("Test User")
                .build();

        when(userService.createUser(any(UserCreateDto.class)))
                .thenThrow(new RuntimeException("Email is required"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(invalidDto));

        assertNotNull(exception.getMessage());
    }

    @Test
    void register_WithNullPassword_ShouldThrowException() {
        // Arrange
        UserCreateDto invalidDto = UserCreateDto.builder()
                .username("testuser")
                .email("test@example.com")
                .password(null)
                .fullName("Test User")
                .build();

        when(userService.createUser(any(UserCreateDto.class)))
                .thenThrow(new RuntimeException("Password is required"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(invalidDto));

        assertNotNull(exception.getMessage());
    }

    @Test
    void login_WithNullUsername_ShouldThrowException() {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest(null, "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Username is required"));

        // Act & Assert
        assertThrows(BadCredentialsException.class,
                () -> authService.login(invalidRequest));
    }

    @Test
    void login_WithNullPassword_ShouldThrowException() {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest("testuser", null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Password is required"));

        // Act & Assert
        assertThrows(BadCredentialsException.class,
                () -> authService.login(invalidRequest));
    }
}