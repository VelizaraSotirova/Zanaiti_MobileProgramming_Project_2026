package bg.zanaiti.zanaiti_api.dto.Craft;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCraftDto {
    private Map<String, CraftTranslationDto> translations;
    private String imageUrl;
    private String animationUrl;
    private Double latitude;
    private Double longitude;
}