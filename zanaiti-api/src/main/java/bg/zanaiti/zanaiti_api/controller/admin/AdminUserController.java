package bg.zanaiti.zanaiti_api.controller.admin;

import bg.zanaiti.zanaiti_api.dto.User.UserDto;
import bg.zanaiti.zanaiti_api.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PostMapping("/{userId}/add-points")
    public ResponseEntity<UserDto> addPointsToUser(
            @PathVariable Long userId,
            @RequestParam int points) {
        return ResponseEntity.ok(adminUserService.addPointsToUser(userId, points));
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserDto> updateUserRoles(
            @PathVariable Long userId,
            @RequestBody Set<String> roles) {
        return ResponseEntity.ok(adminUserService.updateUserRole(userId, roles));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}