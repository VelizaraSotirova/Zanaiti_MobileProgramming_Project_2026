package bg.zanaiti.zanaiti_api.dto.QuizQuestionDto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionCreateDto {
    private Long craftId;
    private Map<String, QuizQuestionTranslationDto> translations;
    private Integer correctOptionIndex;
    private Integer pointsReward;
}