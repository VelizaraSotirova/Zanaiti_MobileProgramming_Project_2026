package bg.zanaiti.zanaiti_api.dto.QuizQuestionDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionTranslationDto {
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}