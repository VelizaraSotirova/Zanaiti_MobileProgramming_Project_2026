package bg.zanaiti.zanaiti_api.dto.Leaderboard;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryDto {
    private Long userId;
    private String username;
    private int totalPoints;
}