package bg.zanaiti.zanaiti_api.controller.publics;

import bg.zanaiti.zanaiti_api.dto.User.UserCreateDto;
import bg.zanaiti.zanaiti_api.security.dto.LoginRequest;
import bg.zanaiti.zanaiti_api.security.dto.LoginResponse;
import bg.zanaiti.zanaiti_api.service.publics.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody UserCreateDto request) {
        return ResponseEntity.ok(authService.register(request));
    }
}