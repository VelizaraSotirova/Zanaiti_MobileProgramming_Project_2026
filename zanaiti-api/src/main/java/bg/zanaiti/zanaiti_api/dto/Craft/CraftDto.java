package bg.zanaiti.zanaiti_api.dto.Craft;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CraftDto {
    private Long id;
    private Map<String, CraftTranslationDto> translations;  // Key: lang ('bg', 'en'), value: translate
    private String imageUrl;
    private String animationUrl;
    private Double latitude;
    private Double longitude;
    private Boolean isActive;
}

