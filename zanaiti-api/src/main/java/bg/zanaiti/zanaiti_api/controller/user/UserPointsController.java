package bg.zanaiti.zanaiti_api.controller.user;

import bg.zanaiti.zanaiti_api.dto.PointsHistory.PointsHistoryDto;
import bg.zanaiti.zanaiti_api.security.CustomUserDetails;
import bg.zanaiti.zanaiti_api.service.user.UserPointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class UserPointsController {

    private final UserPointsService pointsService;

    @GetMapping("/me")
    public ResponseEntity<List<PointsHistoryDto>> getMyHistory(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(pointsService.getUserHistory(currentUser.getUser().getId()));
    }

    @GetMapping("/me/last-days")
    public ResponseEntity<List<PointsHistoryDto>> getMyHistoryLastDays(
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(pointsService.getUserHistoryLastDays(currentUser.getUser().getId(), days));
    }

    @GetMapping("/me/total-last-days")
    public ResponseEntity<Map<String, Object>> getMyTotalPointsLastDays(
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Integer total = pointsService.getTotalPointsLastDays(currentUser.getUser().getId(), days);
        return ResponseEntity.ok(Map.of(
                "userId", currentUser.getUser().getId(),
                "days", days,
                "totalPoints", total
        ));
    }
}