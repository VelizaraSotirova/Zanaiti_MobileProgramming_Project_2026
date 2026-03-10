package bg.zanaiti.zanaiti_api.service.admin;

import bg.zanaiti.zanaiti_api.dto.User.UserDto;
import bg.zanaiti.zanaiti_api.model.Role;
import bg.zanaiti.zanaiti_api.model.User;
import bg.zanaiti.zanaiti_api.repository.RoleRepository;
import bg.zanaiti.zanaiti_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    private User testUser;
    private User adminUser;
    private Role userRole;
    private Role adminRole;
    private final Long userId = 1L;
    private final Long adminId = 2L;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .id(1L)
                .name("USER")
                .description("Regular user")
                .build();

        adminRole = Role.builder()
                .id(2L)
                .name("ADMIN")
                .description("Administrator")
                .build();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .password("encodedPassword")
                .totalPoints(100)
                .createdAt(LocalDateTime.now().minusDays(30))
                .isActive(true)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        adminUser = User.builder()
                .id(adminId)
                .username("admin")
                .email("admin@example.com")
                .fullName("Admin User")
                .password("encodedAdminPassword")
                .totalPoints(500)
                .createdAt(LocalDateTime.now().minusDays(60))
                .isActive(true)
                .roles(new HashSet<>(Set.of(userRole, adminRole)))
                .build();
    }

    // ============= getAllUsers() TEST =============

    @Test
    void getAllUsers_ShouldReturnAllUsers_WhenUsersExist() {
        // Arrange
        List<User> users = List.of(testUser, adminUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserDto> result = adminUserService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        UserDto firstUser = result.getFirst();
        assertEquals(userId, firstUser.getId());
        assertEquals("testuser", firstUser.getUsername());
        assertEquals("test@example.com", firstUser.getEmail());
        assertEquals("Test User", firstUser.getFullName());
        assertEquals(100, firstUser.getTotalPoints());
        assertTrue(firstUser.getRoles().contains("USER"));
        assertTrue(firstUser.isActive());

        UserDto secondUser = result.get(1);
        assertEquals(adminId, secondUser.getId());
        assertEquals("admin", secondUser.getUsername());
        assertTrue(secondUser.getRoles().contains("USER"));
        assertTrue(secondUser.getRoles().contains("ADMIN"));

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsersExist() {
        // Arrange
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<UserDto> result = adminUserService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    // ============= getUserById() TESTS =============

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserDto result = adminUserService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName());
        assertEquals(100, result.getTotalPoints());
        assertTrue(result.getRoles().contains("USER"));
        assertTrue(result.isActive());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminUserService.getUserById(999L));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
    }

    // ============= addPointsToUser() TESTS =============

    @Test
    void addPointsToUser_ShouldIncreaseUserPoints_WhenUserExists() {
        // Arrange
        int initialPoints = testUser.getTotalPoints();
        int pointsToAdd = 50;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = adminUserService.addPointsToUser(userId, pointsToAdd);

        // Assert
        assertNotNull(result);
        assertEquals(initialPoints + pointsToAdd, testUser.getTotalPoints());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void addPointsToUser_ShouldHandleZeroPoints_WhenZeroProvided() {
        // Arrange
        int initialPoints = testUser.getTotalPoints();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = adminUserService.addPointsToUser(userId, 0);

        // Assert
        assertNotNull(result);
        assertEquals(initialPoints, testUser.getTotalPoints()); // no change
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void addPointsToUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminUserService.addPointsToUser(999L, 50));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // ============= deleteUser() TESTS =============

    @Test
    void deleteUser_ShouldDeactivateUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        adminUserService.deleteUser(userId);

        // Assert
        assertFalse(testUser.isActive()); // user is deactivated
        assertNotNull(testUser.getUpdatedAt());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminUserService.deleteUser(999L));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // ============= updateUserRole() TESTS =============

    @Test
    void updateUserRole_ShouldUpdateRoles_WhenUserExistsAndRolesExist() {
        // Arrange
        Set<String> newRoleNames = Set.of("USER", "ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = adminUserService.updateUserRole(userId, newRoleNames);

        // Assert
        assertNotNull(result);
        assertTrue(testUser.getRoles().contains(userRole));
        assertTrue(testUser.getRoles().contains(adminRole));
        assertEquals(2, testUser.getRoles().size());

        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findByName("USER");
        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateUserRole_ShouldRemoveOldRolesAndAddNewOnes() {
        // Arrange
        Set<String> newRoleNames = Set.of("ADMIN"); // remove USER, add ADMIN

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = adminUserService.updateUserRole(userId, newRoleNames);

        // Assert
        assertNotNull(result);
        assertFalse(testUser.getRoles().contains(userRole)); // USER role removed
        assertTrue(testUser.getRoles().contains(adminRole)); // ADMIN role added
        assertEquals(1, testUser.getRoles().size());

        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(roleRepository, never()).findByName("USER");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateUserRole_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        Set<String> newRoleNames = Set.of("ADMIN");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminUserService.updateUserRole(999L, newRoleNames));

        assertEquals("User not found", exception.getMessage());
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_ShouldThrowException_WhenRoleNotFound() {
        // Arrange
        Set<String> newRoleNames = Set.of("NON_EXISTENT_ROLE");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("NON_EXISTENT_ROLE")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminUserService.updateUserRole(userId, newRoleNames));

        assertEquals("Role not found: NON_EXISTENT_ROLE", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_ShouldHandleEmptyRoleSet() {
        // Arrange
        Set<String> emptyRoles = new HashSet<>();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = adminUserService.updateUserRole(userId, emptyRoles);

        // Assert
        assertNotNull(result);
        assertTrue(testUser.getRoles().isEmpty()); // all roles removed
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, times(1)).save(testUser);
    }

    // ============= convertToDto TESTS (indirectly) =============

    @Test
    void convertToDto_ShouldHandleUserWithNoRoles() {
        // Arrange
        User userWithNoRoles = User.builder()
                .id(3L)
                .username("noroles")
                .email("noroles@example.com")
                .fullName("No Roles User")
                .totalPoints(0)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .roles(new HashSet<>()) // empty roles
                .build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(userWithNoRoles));

        // Act
        UserDto result = adminUserService.getUserById(3L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());
    }

    @Test
    void convertToDto_ShouldHandleInactiveUser() {
        // Arrange
        User inactiveUser = User.builder()
                .id(4L)
                .username("inactive")
                .email("inactive@example.com")
                .fullName("Inactive User")
                .totalPoints(50)
                .createdAt(LocalDateTime.now())
                .isActive(false) // inactive user
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        when(userRepository.findById(4L)).thenReturn(Optional.of(inactiveUser));

        // Act
        UserDto result = adminUserService.getUserById(4L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isActive());
    }
}