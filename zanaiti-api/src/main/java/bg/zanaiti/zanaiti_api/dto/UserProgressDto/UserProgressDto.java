package bg.zanaiti.zanaiti_api.dto.UserProgressDto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressDto {
    private Long id;
    private Long userId;
    private Long craftId;
    private String craftName;
    private int pointsEarned;
    private boolean quizCompleted;
    private int attemptCount;
    private LocalDateTime lastInteractionDate;
    private LocalDateTime quizCompletionDate;
    private Integer quizScore;
    private String languageCode;
}
