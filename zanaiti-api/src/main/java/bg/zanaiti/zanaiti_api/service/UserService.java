//package bg.zanaiti.zanaiti_api.service;
//
//import bg.zanaiti.zanaiti_api.dto.User.UserCreateDto;
//import bg.zanaiti.zanaiti_api.dto.User.UserDto;
//import bg.zanaiti.zanaiti_api.model.Role;
//import bg.zanaiti.zanaiti_api.model.User;
//import bg.zanaiti.zanaiti_api.repository.RoleRepository;
//import bg.zanaiti.zanaiti_api.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class UserService {
//
//    private final UserRepository userRepository;
//    private final RoleRepository roleRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    // AuthService (register)
//    @Transactional
//    public UserDto createUser(UserCreateDto createDto) {
////        if (userRepository.existsByUsername(createDto.getUsername())) {
////            throw new RuntimeException("Username already exists");
////        }
////        if (userRepository.existsByEmail(createDto.getEmail())) {
////            throw new RuntimeException("Email already exists");
////        }
////
////        User user = User.builder()
////                .username(createDto.getUsername())
////                .email(createDto.getEmail())
////                .fullName(createDto.getFullName())
////                .password(passwordEncoder.encode(createDto.getPassword()))
////                .totalPoints(0)
////                .createdAt(LocalDateTime.now())
////                .isActive(true)
////                .build();
////
////        Role userRole = roleRepository.findByName("USER")
////                .orElseThrow(() -> new RuntimeException("Default role USER not found"));
////
////        user.setRoles(Set.of(userRole));
////
////        User savedUser = userRepository.save(user);
////        return convertToDto(savedUser);
//
//        // Променяме RuntimeException на DataIntegrityViolationException
//        if (userRepository.existsByUsername(createDto.getUsername())) {
//            throw new org.springframework.dao.DataIntegrityViolationException("Потребителското име вече е заето!");
//        }
//        if (userRepository.existsByEmail(createDto.getEmail())) {
//            throw new org.springframework.dao.DataIntegrityViolationException("Имейл адресът вече е регистриран!");
//        }
//
//        // Търсене на ролята - тук може да остане RuntimeException,
//        // защото това е системна грешка (липсваща конфигурация в БД)
//        Role userRole = roleRepository.findByName("USER")
//                .orElseThrow(() -> new RuntimeException("Грешка: Ролята USER не е намерена в базата данни!"));
//
//        User user = User.builder()
//                .username(createDto.getUsername())
//                .email(createDto.getEmail())
//                .fullName(createDto.getFullName())
//                .password(passwordEncoder.encode(createDto.getPassword()))
//                .totalPoints(0)
//                .isActive(true)
//                .createdAt(LocalDateTime.now())
//                .roles(Set.of(userRole)) // Директно сетваме ролята тук
//                .build();
//
//        User savedUser = userRepository.save(user);
//        return convertToDto(savedUser);
//    }
//
//    // AuthService (login)
//    public UserDto getUserByUsername(String username) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
//        return convertToDto(user);
//    }
//
//    // Shared method - used by many services
//    public UserDto getUserById(Long id) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
//        return convertToDto(user);
//    }
//
//    public boolean existsByEmail(String email) {
//        return userRepository.existsByEmail(email);
//    }
//
//    public boolean existsByUsername(String username) {
//        return userRepository.existsByUsername(username);
//    }
//
//    // Converter
//    public UserDto convertToDto(User user) {
//        Set<String> roleNames = user.getRoles() != null ?
//                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()) :
//                new HashSet<>();
//
//        return UserDto.builder()
//                .id(user.getId())
//                .username(user.getUsername())
//                .email(user.getEmail())
//                .fullName(user.getFullName())
//                .totalPoints(user.getTotalPoints())
//                .createdAt(user.getCreatedAt())
//                .roles(roleNames)
//                .isActive(user.isActive())
//                .build();
//    }
//}

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

    /**
     * Създава нов потребител.
     * Валидацията за дублиращи се данни вече е минала в AuthService.
     */
    @Transactional
    public UserDto createUser(UserCreateDto createDto) {
        // Намираме ролята USER. Ако я няма, хвърляме грешка (това е системен проблем).
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

    // Използва се в AuthService за проверка преди регистрация
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Използва се в AuthService за проверка преди регистрация
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // Използва се в AuthService при Login
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Потребителят не е намерен: " + username));
        return convertToDto(user);
    }

    // Използва се от други услуги (напр. ProfileService)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Потребител с ID " + id + " не е намерен"));
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