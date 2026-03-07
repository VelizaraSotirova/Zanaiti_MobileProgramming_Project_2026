package bg.zanaiti.zanaiti_api.dto.UserProgressDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressCreateDto {
    private Long userId;
    private Long craftId;
    private int pointsEarned; // default: 0
}
