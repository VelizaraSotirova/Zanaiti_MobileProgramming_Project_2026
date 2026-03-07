package bg.zanaiti.zanaiti_api.service.admin;

import bg.zanaiti.zanaiti_api.dto.Craft.CraftDto;
import bg.zanaiti.zanaiti_api.dto.Craft.CraftTranslationDto;
import bg.zanaiti.zanaiti_api.dto.Craft.CreateCraftDto;
import bg.zanaiti.zanaiti_api.exceptionHandlers.DuplicateCraftException;
import bg.zanaiti.zanaiti_api.model.*;
import bg.zanaiti.zanaiti_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCraftService {

    private final CraftRepository craftRepository;
    private final CraftTranslationRepository translationRepository;
    private final LanguageRepository languageRepository;
    private final UserRepository userRepository;

    @Transactional
    public CraftDto createCraft(CreateCraftDto dto, Long adminUserId) {
        String bulgarianName = dto.getTranslations().get("bg").getName();
        if (translationRepository.existsByNameAndLanguageCode(bulgarianName, "bg")) {
            throw new DuplicateCraftException("Craft with name '" + bulgarianName + "' already exists!");
        }

        Craft craft = Craft.builder()
                .imageUrl(dto.getImageUrl())
                .animationUrl(dto.getAnimationUrl())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy(userRepository.getReferenceById(adminUserId))
                .translations(new ArrayList<>())
                .build();

        Craft savedCraft = craftRepository.save(craft);
        saveTranslations(savedCraft, dto.getTranslations());
        return mapToDto(savedCraft);
    }

    @Transactional
    public CraftDto updateCraft(Long id, CraftDto dto) {
        Craft craft = craftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Craft not found with id: " + id));

        craft.setImageUrl(dto.getImageUrl());
        craft.setAnimationUrl(dto.getAnimationUrl());
        craft.setLatitude(dto.getLatitude());
        craft.setLongitude(dto.getLongitude());
        craft.setActive(dto.getIsActive());
        craft.setUpdatedAt(LocalDateTime.now());

        if (dto.getTranslations() != null) {
            updateTranslations(craft, dto.getTranslations());
        }

        return mapToDto(craftRepository.save(craft));
    }

    @Transactional
    public void deleteCraft(Long id) {
        Craft craft = craftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Craft not found with id: " + id));
        craft.setActive(false);
        craft.setUpdatedAt(LocalDateTime.now());
        craftRepository.save(craft);
    }

    private void saveTranslations(Craft craft, Map<String, CraftTranslationDto> translations) {
        for (Map.Entry<String, CraftTranslationDto> entry : translations.entrySet()) {
            String languageCode = entry.getKey();
            CraftTranslationDto dto = entry.getValue();

            Language language = languageRepository.findByCode(languageCode)
                    .orElseThrow(() -> new RuntimeException("Language not found: " + languageCode));

            CraftTranslation translation = CraftTranslation.builder()
                    .craft(craft)
                    .language(language)
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .historicalFacts(dto.getHistoricalFacts())
                    .makingProcess(dto.getMakingProcess())
                    .build();

            translationRepository.save(translation);

            if (craft.getTranslations() == null) {
                craft.setTranslations(new ArrayList<>());
            }
            craft.getTranslations().add(translation);
        }
    }

    private void updateTranslations(Craft craft, Map<String, CraftTranslationDto> translations) {
        for (Map.Entry<String, CraftTranslationDto> entry : translations.entrySet()) {
            String languageCode = entry.getKey();
            CraftTranslationDto dto = entry.getValue();

            translationRepository.findByCraftIdAndLanguageCode(craft.getId(), languageCode)
                    .ifPresentOrElse(
                            translation -> {
                                translation.setName(dto.getName());
                                translation.setDescription(dto.getDescription());
                                translation.setHistoricalFacts(dto.getHistoricalFacts());
                                translation.setMakingProcess(dto.getMakingProcess());
                                translationRepository.save(translation);
                            },
                            () -> {
                                Language language = languageRepository.findByCode(languageCode)
                                        .orElseThrow(() -> new RuntimeException("Language not found"));
                                CraftTranslation newTranslation = CraftTranslation.builder()
                                        .craft(craft)
                                        .language(language)
                                        .name(dto.getName())
                                        .description(dto.getDescription())
                                        .historicalFacts(dto.getHistoricalFacts())
                                        .makingProcess(dto.getMakingProcess())
                                        .build();
                                translationRepository.save(newTranslation);
                            }
                    );
        }
    }

    private CraftDto mapToDto(Craft craft) {
        Map<String, CraftTranslationDto> translationsMap = craft.getTranslations().stream()
                .collect(Collectors.toMap(
                        t -> t.getLanguage().getCode(),
                        this::mapTranslationToDto
                ));

        // Check is the list is null
        if (craft.getTranslations() != null) {
            translationsMap = craft.getTranslations().stream()
                    .collect(Collectors.toMap(
                            t -> t.getLanguage().getCode(),
                            this::mapTranslationToDto
                    ));
        }

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

    private CraftTranslationDto mapTranslationToDto(CraftTranslation translation) {
        return CraftTranslationDto.builder()
                .name(translation.getName())
                .description(translation.getDescription())
                .historicalFacts(translation.getHistoricalFacts())
                .makingProcess(translation.getMakingProcess())
                .build();
    }
}