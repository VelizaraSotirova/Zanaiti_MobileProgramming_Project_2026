package bg.zanaiti.zanaiti_api.service.publics;

import bg.zanaiti.zanaiti_api.dto.Craft.CraftDto;
import bg.zanaiti.zanaiti_api.dto.Craft.CraftTranslationDto;
import bg.zanaiti.zanaiti_api.model.Craft;
import bg.zanaiti.zanaiti_api.model.CraftTranslation;
import bg.zanaiti.zanaiti_api.repository.CraftRepository;
import bg.zanaiti.zanaiti_api.repository.CraftTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicCraftService {

    private final CraftRepository craftRepository;
    private final CraftTranslationRepository translationRepository;

    public List<CraftDto> getAllCrafts() {
        return craftRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CraftDto getCraftById(Long id) {
        Craft craft = craftRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Craft not found or not active"));
        return mapToDto(craft);
    }

    public CraftDto getCraftByIdAndLanguage(Long id, String languageCode) {
        Craft craft = craftRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Craft not found or not active"));

        CraftDto dto = mapToBasicDto(craft);

        Optional<CraftTranslation> translation = translationRepository
                .findByCraftIdAndLanguageCode(id, languageCode);

        translation.ifPresent(t -> {
            Map<String, CraftTranslationDto> translations = Map.of(
                    languageCode, mapTranslationToDto(t)
            );
            dto.setTranslations(translations);
        });

        return dto;
    }

    public List<CraftDto> getCraftsInRadius(double latitude, double longitude, double radius) {
        return craftRepository.findCraftsWithinRadius(latitude, longitude, radius)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CraftDto mapToDto(Craft craft) {
        Map<String, CraftTranslationDto> translationsMap = craft.getTranslations().stream()
                .collect(Collectors.toMap(
                        t -> t.getLanguage().getCode(),
                        this::mapTranslationToDto
                ));

        return CraftDto.builder()
                .id(craft.getId())
                .translations(translationsMap)
                .imageUrl(craft.getImageUrl())
                .animationUrl(craft.getAnimationUrl())
                .latitude(craft.getLatitude())
                .longitude(craft.getLongitude())
                .isActive(craft.isActive())
                .build();
    }

    private CraftDto mapToBasicDto(Craft craft) {
        return CraftDto.builder()
                .id(craft.getId())
                .imageUrl(craft.getImageUrl())
                .animationUrl(craft.getAnimationUrl())
                .latitude(craft.getLatitude())
                .longitude(craft.getLongitude())
                .isActive(craft.isActive())
                .build();
    }

    private CraftTranslationDto mapTranslationToDto(CraftTranslation translation) {
        return CraftTranslationDto.builder()
                .name(translation.getName())
                .description(translation.getDescription())
                .historicalFacts(translation.getHistoricalFacts())
                .makingProcess(translation.getMakingProcess())
                .build();
    }
}