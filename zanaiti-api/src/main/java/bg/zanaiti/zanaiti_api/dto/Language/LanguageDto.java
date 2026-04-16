package bg.zanaiti.zanaiti_api.dto.Language;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LanguageDto {
    private Long id;
    private String code;  // 'bg', 'en'
    private String name;  // 'български', 'English'
    private boolean isActive;
}