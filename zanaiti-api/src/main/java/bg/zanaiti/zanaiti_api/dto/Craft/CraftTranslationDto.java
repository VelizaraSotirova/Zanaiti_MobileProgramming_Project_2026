package bg.zanaiti.zanaiti_api.dto.Craft;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CraftTranslationDto {
    private String name;
    private String description;
    private String historicalFacts;
    private String makingProcess;
}
