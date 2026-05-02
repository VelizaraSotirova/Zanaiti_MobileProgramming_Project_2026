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


    @Transactional
    public UserDto createUser(UserCreateDto createDto) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Грешка: Ролята USER не е намерена в базата данни!"));

        User user = User.builder()
                .username(createDto.getUsername())
                .email(createDto.getEmail())
                .fullName(createDto.getFullName())
                .password(passwordEncoder.encode(createDto.getPassword()))
                .totalPoints(0)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    // Използват се в AuthService за проверка преди регистрация
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // Използва се в AuthService при Login
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Потребителят не е намерен: " + username));
        return convertToDto(user);
    }


    public UserDto convertToDto(User user) {
        Set<String> roleNames = user.getRoles() != null ?
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()) :
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