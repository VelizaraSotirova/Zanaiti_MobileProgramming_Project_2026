package bg.zanaiti.zanaiti_api.service.publics;

import bg.zanaiti.zanaiti_api.dto.User.UserCreateDto;
import bg.zanaiti.zanaiti_api.dto.User.UserDto;
import bg.zanaiti.zanaiti_api.security.CustomUserDetails;
import bg.zanaiti.zanaiti_api.security.dto.LoginRequest;
import bg.zanaiti.zanaiti_api.security.dto.LoginResponse;
import bg.zanaiti.zanaiti_api.security.jwt.JwtService;
import bg.zanaiti.zanaiti_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        UserDto user = userService.getUserByUsername(request.getUsername());

        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .totalPoints(user.getTotalPoints())
                .role(user.getRoles().stream().findFirst().orElse("USER"))
                .build();
    }


    public LoginResponse register(UserCreateDto createDto) {
        if (userService.existsByEmail(createDto.getEmail())) {
            throw new DataIntegrityViolationException("Имейл адресът вече е регистриран");
        }
        if (userService.existsByUsername(createDto.getUsername())) {
            throw new DataIntegrityViolationException("Потребителското име вече е заето");
        }


        UserDto user = userService.createUser(createDto);


        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .totalPoints(user.getTotalPoints())
                .role(user.getRoles().stream().findFirst().orElse("USER"))
                .build();
    }
}