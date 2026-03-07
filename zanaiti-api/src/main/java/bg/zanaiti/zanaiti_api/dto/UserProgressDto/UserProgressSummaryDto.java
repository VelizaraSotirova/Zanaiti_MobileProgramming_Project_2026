package bg.zanaiti.zanaiti_api.dto.UserProgressDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressSummaryDto {
    private int totalPoints;
    private int craftsVisited;
    private int quizzesCompleted;
    private double averageScore;
}