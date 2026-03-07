package bg.zanaiti.zanaiti_api.controller.user;

import bg.zanaiti.zanaiti_api.dto.User.UserDto;
import bg.zanaiti.zanaiti_api.dto.User.UserUpdateDto;
import bg.zanaiti.zanaiti_api.security.CustomUserDetails;
import bg.zanaiti.zanaiti_api.service.user.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(profileService.getUserProfile(currentUser.getUser().getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMyProfile(
            @RequestBody UserUpdateDto updateDto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(profileService.updateUserProfile(currentUser.getUser().getId(), updateDto));
    }
}