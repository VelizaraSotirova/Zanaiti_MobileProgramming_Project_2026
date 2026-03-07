package bg.zanaiti.zanaiti_api.service;

import bg.zanaiti.zanaiti_api.dto.User.UserCreateDto;
import bg.zanaiti.zanaiti_api.dto.User.UserDto;
import bg.zanaiti.zanaiti_api.model.Role;
import bg.zanaiti.zanaiti_api.model.User;
import bg.zanaiti.zanaiti_api.repository.RoleRepository;
import bg.zanaiti.zanaiti_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // AuthService (register)
    @Transactional
    public UserDto createUser(UserCreateDto createDto) {
        if (userRepository.existsByUsername(createDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(createDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(createDto.getUsername())
                .email(createDto.getEmail())
                .fullName(createDto.getFullName())
                .password(passwordEncoder.encode(createDto.getPassword()))
                .totalPoints(0)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        user.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    // AuthService (login)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return convertToDto(user);
    }

    // Shared method - used by many services
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDto(user);
    }

    // Converter
    public UserDto convertToDto(User user) {
        Set<String> roleNames = user.getRoles() != null ?
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()) :
                new HashSet<>();

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .totalPoints(user.getTotalPoints())
                .createdAt(user.getCreatedAt())
                .roles(roleNames)
                .isActive(user.isActive())
                .build();
    }
}