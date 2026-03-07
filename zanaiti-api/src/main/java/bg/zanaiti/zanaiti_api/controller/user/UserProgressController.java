package bg.zanaiti.zanaiti_api.controller.user;

import bg.zanaiti.zanaiti_api.dto.LeaderboardEntryDto;
import bg.zanaiti.zanaiti_api.dto.UserProgressDto.UserProgressDto;
import bg.zanaiti.zanaiti_api.security.CustomUserDetails;
import bg.zanaiti.zanaiti_api.service.user.UserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class UserProgressController {

    private final UserProgressService progressService;

    @GetMapping("/me")
    public ResponseEntity<List<UserProgressDto>> getMyProgress(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getUser().getId();
        return ResponseEntity.ok(progressService.getUserProgress(userId));
    }

    @GetMapping("/me/craft/{craftId}")
    public ResponseEntity<UserProgressDto> getMyProgressForCraft(
            @PathVariable Long craftId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getUser().getId();
        UserProgressDto progress = progressService.getProgressForCraft(userId, craftId);
        return progress != null ? ResponseEntity.ok(progress) : ResponseEntity.notFound().build();
    }

    @PostMapping("/me/craft/{craftId}/visit")
    public ResponseEntity<UserProgressDto> recordVisit(
            @PathVariable Long craftId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getUser().getId();
        return ResponseEntity.ok(progressService.recordVisit(userId, craftId));
    }

    @PostMapping("/me/craft/{craftId}/complete-quiz")
    public ResponseEntity<UserProgressDto> completeQuiz(
            @PathVariable Long craftId,
            @RequestParam int correctAnswersCount,
            @RequestParam(defaultValue = "bg") String lang,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getUser().getId();
        return ResponseEntity.ok(progressService.completeQuiz(userId, craftId, correctAnswersCount, lang));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard() {
        return ResponseEntity.ok(progressService.getLeaderboard());
    }
}