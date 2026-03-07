package bg.zanaiti.zanaiti_api.dto.PointsHistory;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsHistoryDto {
    private Long id;
    private Long userId;
    private String username;
    private int points;
    private String source;      // "QUIZ", "VISIT", "BONUS"
    private String description;
    private LocalDateTime createdAt;
    private Long craftId;
    private String craftName;
}
