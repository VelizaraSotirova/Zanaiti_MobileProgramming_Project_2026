package bg.zanaiti.zanaiti_api.service.admin;

import bg.zanaiti.zanaiti_api.dto.Craft.CraftDto;
import bg.zanaiti.zanaiti_api.dto.Craft.CraftTranslationDto;
import bg.zanaiti.zanaiti_api.dto.Craft.CreateCraftDto;
import bg.zanaiti.zanaiti_api.exceptionHandlers.DuplicateCraftException;
import bg.zanaiti.zanaiti_api.model.*;
import bg.zanaiti.zanaiti_api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCraftServiceTest {

    @Mock
    private CraftRepository craftRepository;

    @Mock
    private CraftTranslationRepository translationRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminCraftService adminCraftService;

    private User adminUser;
    private Language bgLanguage;
    private Language enLanguage;
    private Craft testCraft;
    private CraftTranslation bgTranslation;
    private CraftTranslation enTranslation;
    private final Long adminId = 1L;
    private final Long craftId = 1L;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(adminId)
                .username("admin")
                .email("admin@example.com")
                .build();

        bgLanguage = Language.builder()
                .id(1L)
                .code("bg")
                .name("български")
                .build();

        enLanguage = Language.builder()
                .id(2L)
                .code("en")
                .name("English")
                .build();

        testCraft = Craft.builder()
                .id(craftId)
                .imageUrl("craft.jpg")
                .animationUrl("animation.mp4")
                .latitude(42.0)
                .longitude(24.0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy(adminUser)
                .translations(new ArrayList<>())
                .build();

        bgTranslation = CraftTranslation.builder()
                .id(1L)
                .craft(testCraft)
                .language(bgLanguage)
                .name("Грънчарство")
                .description("Описание на български")
                .historicalFacts("Исторически факти на бг")
                .makingProcess("Процес на бг")
                .build();

        enTranslation = CraftTranslation.builder()
                .id(2L)
                .craft(testCraft)
                .language(enLanguage)
                .name("Pottery")
                .description("Description in English")
                .historicalFacts("Historical facts in en")
                .makingProcess("Making process in en")
                .build();

        testCraft.getTranslations().add(bgTranslation);
        testCraft.getTranslations().add(enTranslation);
    }

    // ============= createCraft() TEST =============

    @Test
    void createCraft_ShouldCreateCraft_WhenDataIsValid() {
        // Arrange
        CreateCraftDto createDto = createValidCreateCraftDto();

        when(translationRepository.existsByNameAndLanguageCode("Нов занаят", "bg"))
                .thenReturn(false);
        when(userRepository.getReferenceById(adminId)).thenReturn(adminUser);
        when(craftRepository.save(any(Craft.class))).thenAnswer(i -> {
            Craft saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(languageRepository.findByCode("bg")).thenReturn(Optional.of(bgLanguage));
        when(languageRepository.findByCode("en")).thenReturn(Optional.of(enLanguage));
        when(translationRepository.save(any(CraftTranslation.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        CraftDto result = adminCraftService.createCraft(createDto, adminId);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("new-image.jpg", result.getImageUrl());
        assertEquals(42.5, result.getLatitude());
        assertEquals(24.5, result.getLongitude());
        assertTrue(result.getIsActive());

        // Translations check
        assertNotNull(result.getTranslations());
        assertEquals(2, result.getTranslations().size());
        assertTrue(result.getTranslations().containsKey("bg"));
        assertTrue(result.getTranslations().containsKey("en"));

        CraftTranslationDto bgTrans = result.getTranslations().get("bg");
        assertEquals("Нов занаят", bgTrans.getName());
        assertEquals("Ново описание на бг", bgTrans.getDescription());

        CraftTranslationDto enTrans = result.getTranslations().get("en");
        assertEquals("New craft", enTrans.getName());
        assertEquals("New description in en", enTrans.getDescription());

        verify(translationRepository, times(1)).existsByNameAndLanguageCode("Нов занаят", "bg");
        verify(userRepository, times(1)).getReferenceById(adminId);
        verify(craftRepository, times(1)).save(any(Craft.class));
        verify(languageRepository, times(1)).findByCode("bg");
        verify(languageRepository, times(1)).findByCode("en");
        verify(translationRepository, times(2)).save(any(CraftTranslation.class));
    }

    @Test
    void createCraft_ShouldThrowDuplicateCraftException_WhenCraftWithSameNameExists() {
        // Arrange
        CreateCraftDto createDto = createValidCreateCraftDto();

        when(translationRepository.existsByNameAndLanguageCode("Нов занаят", "bg"))
                .thenReturn(true); // already exists

        // Act & Assert
        DuplicateCraftException exception = assertThrows(DuplicateCraftException.class,
                () -> adminCraftService.createCraft(createDto, adminId));

        assertEquals("Craft with name 'Нов занаят' already exists!", exception.getMessage());

        verify(translationRepository, times(1)).existsByNameAndLanguageCode("Нов занаят", "bg");
        verify(userRepository, never()).getReferenceById(anyLong());
        verify(craftRepository, never()).save(any(Craft.class));
    }

    @Test
    void createCraft_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        CreateCraftDto createDto = createValidCreateCraftDto();

        when(translationRepository.existsByNameAndLanguageCode("Нов занаят", "bg"))
                .thenReturn(false);
        when(userRepository.getReferenceById(adminId)).thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminCraftService.createCraft(createDto, adminId));

        assertEquals("User not found", exception.getMessage());
        verify(craftRepository, never()).save(any(Craft.class));
    }

    @Test
    void createCraft_ShouldThrowException_WhenLanguageNotFound() {
        // Arrange
        CreateCraftDto createDto = createValidCreateCraftDto();

        when(translationRepository.existsByNameAndLanguageCode("Нов занаят", "bg"))
                .thenReturn(false);
        when(userRepository.getReferenceById(adminId)).thenReturn(adminUser);
        when(craftRepository.save(any(Craft.class))).thenAnswer(i -> {
            Craft saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(languageRepository.findByCode("bg")).thenReturn(Optional.empty()); // language not found

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminCraftService.createCraft(createDto, adminId));

        assertEquals("Language not found: bg", exception.getMessage());
        verify(translationRepository, never()).save(any(CraftTranslation.class));
    }

    // ============= updateCraft() TESTS =============

    @Test
    void updateCraft_ShouldUpdateCraft_WhenDataIsValid() {
        // Arrange
        CraftDto updateDto = createValidCraftDto();

        when(craftRepository.findById(craftId)).thenReturn(Optional.of(testCraft));
        when(translationRepository.findByCraftIdAndLanguageCode(craftId, "bg"))
                .thenReturn(Optional.of(bgTranslation));
        when(translationRepository.findByCraftIdAndLanguageCode(craftId, "en"))
                .thenReturn(Optional.of(enTranslation));
        when(craftRepository.save(any(Craft.class))).thenReturn(testCraft);

        // Act
        CraftDto result = adminCraftService.updateCraft(craftId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(craftId, result.getId());
        assertEquals("updated-image.jpg", result.getImageUrl());
        assertEquals(43.0, result.getLatitude());
        assertEquals(25.0, result.getLongitude());
        assertFalse(result.getIsActive());

        // Check that translations are updated
        assertEquals("Обновен занаят", bgTranslation.getName());
        assertEquals("Обновено описание на бг", bgTranslation.getDescription());
        assertEquals("Updated craft", enTranslation.getName());
        assertEquals("Updated description in en", enTranslation.getDescription());

        verify(craftRepository, times(1)).findById(craftId);
        verify(translationRepository, times(1)).findByCraftIdAndLanguageCode(craftId, "bg");
        verify(translationRepository, times(1)).findByCraftIdAndLanguageCode(craftId, "en");
        verify(translationRepository, times(2)).save(any(CraftTranslation.class));
        verify(craftRepository, times(1)).save(any(Craft.class));
    }

    @Test
    void updateCraft_ShouldCreateNewTranslations_WhenTheyDidNotExist() {
        // Arrange
        CraftDto updateDto = createValidCraftDto();

        when(craftRepository.findById(craftId)).thenReturn(Optional.of(testCraft));
        when(languageRepository.findByCode("bg")).thenReturn(Optional.of(bgLanguage));
        when(languageRepository.findByCode("en")).thenReturn(Optional.of(enLanguage));
        // translations do not exist
        when(translationRepository.findByCraftIdAndLanguageCode(craftId, "bg"))
                .thenReturn(Optional.empty());
        when(translationRepository.findByCraftIdAndLanguageCode(craftId, "en"))
                .thenReturn(Optional.empty());
        when(craftRepository.save(any(Craft.class))).thenReturn(testCraft);

        // Act
        CraftDto result = adminCraftService.updateCraft(craftId, updateDto);

        // Assert
        assertNotNull(result);
        verify(translationRepository, times(2)).save(any(CraftTranslation.class)); // new translations
    }

    @Test
    void updateCraft_ShouldThrowException_WhenCraftNotFound() {
        // Arrange
        CraftDto updateDto = createValidCraftDto();

        when(craftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminCraftService.updateCraft(999L, updateDto));

        assertEquals("Craft not found with id: 999", exception.getMessage());
        verify(craftRepository, never()).save(any(Craft.class));
    }

    @Test
    void updateCraft_ShouldThrowException_WhenLanguageNotFound() {
        // Arrange
        CraftDto updateDto = createValidCraftDto();

        when(craftRepository.findById(craftId)).thenReturn(Optional.of(testCraft));
        when(languageRepository.findByCode("bg")).thenReturn(Optional.empty()); // language is not found

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminCraftService.updateCraft(craftId, updateDto));

        assertEquals("Language not found", exception.getMessage());
        verify(translationRepository, never()).save(any(CraftTranslation.class));
    }

    @Test
    void updateCraft_ShouldUpdateOnlyProvidedTranslations() {
        // Arrange - only bulgarian translate in DTO
        CraftDto updateDto = CraftDto.builder()
                .id(craftId)
                .imageUrl("updated-image.jpg")
                .latitude(43.0)
                .longitude(25.0)
                .isActive(true)
                .translations(Map.of("bg", CraftTranslationDto.builder()
                        .name("Само бг име")
                        .description("Само бг описание")
                        .build()))
                .build();

        when(craftRepository.findById(craftId)).thenReturn(Optional.of(testCraft));
        when(translationRepository.findByCraftIdAndLanguageCode(craftId, "bg"))
                .thenReturn(Optional.of(bgTranslation));
        when(craftRepository.save(any(Craft.class))).thenReturn(testCraft);

        // Act
        CraftDto result = adminCraftService.updateCraft(craftId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Само бг име", bgTranslation.getName());
        assertEquals("Само бг описание", bgTranslation.getDescription());
        // English translate is unchanged
        assertEquals("Pottery", enTranslation.getName());

        verify(translationRepository, times(1)).findByCraftIdAndLanguageCode(craftId, "bg");
        verify(translationRepository, never()).findByCraftIdAndLanguageCode(craftId, "en");
        verify(translationRepository, times(1)).save(any(CraftTranslation.class));
    }

    // ============= deleteCraft() TEST =============

    @Test
    void deleteCraft_ShouldDeactivateCraft_WhenCraftExists() {
        // Arrange
        when(craftRepository.findById(craftId)).thenReturn(Optional.of(testCraft));
        when(craftRepository.save(any(Craft.class))).thenReturn(testCraft);

        // Act
        adminCraftService.deleteCraft(craftId);

        // Assert
        assertFalse(testCraft.isActive()); // disabled
        assertNotNull(testCraft.getUpdatedAt());

        verify(craftRepository, times(1)).findById(craftId);
        verify(craftRepository, times(1)).save(testCraft);
    }

    @Test
    void deleteCraft_ShouldThrowException_WhenCraftNotFound() {
        // Arrange
        when(craftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminCraftService.deleteCraft(999L));

        assertEquals("Craft not found with id: 999", exception.getMessage());
        verify(craftRepository, never()).save(any(Craft.class));
    }

    @Test
    void deleteCraft_ShouldSetUpdatedAt_WhenDeactivated() {
        // Arrange
        LocalDateTime beforeTest = LocalDateTime.now();
        when(craftRepository.findById(craftId)).thenReturn(Optional.of(testCraft));
        when(craftRepository.save(any(Craft.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        adminCraftService.deleteCraft(craftId);

        // Assert
        assertNotNull(testCraft.getUpdatedAt());
        assertTrue(testCraft.getUpdatedAt().isAfter(beforeTest) ||
                testCraft.getUpdatedAt().isEqual(beforeTest));
    }



    private CreateCraftDto createValidCreateCraftDto() {
        Map<String, CraftTranslationDto> translations = new HashMap<>();

        translations.put("bg", CraftTranslationDto.builder()
                .name("Нов занаят")
                .description("Ново описание на бг")
                .historicalFacts("Нови факти на бг")
                .makingProcess("Нов процес на бг")
                .build());

        translations.put("en", CraftTranslationDto.builder()
                .name("New craft")
                .description("New description in en")
                .historicalFacts("New facts in en")
                .makingProcess("New process in en")
                .build());

        return CreateCraftDto.builder()
                .translations(translations)
                .imageUrl("new-image.jpg")
                .animationUrl("new-animation.mp4")
                .latitude(42.5)
                .longitude(24.5)
                .build();
    }

    private CraftDto createValidCraftDto() {
        Map<String, CraftTranslationDto> translations = new HashMap<>();

        translations.put("bg", CraftTranslationDto.builder()
                .name("Обновен занаят")
                .description("Обновено описание на бг")
                .historicalFacts("Обновени факти на бг")
                .makingProcess("Обновен процес на бг")
                .build());

        translations.put("en", CraftTranslationDto.builder()
                .name("Updated craft")
                .description("Updated description in en")
                .historicalFacts("Updated facts in en")
                .makingProcess("Updated process in en")
                .build());

        return CraftDto.builder()
                .id(craftId)
                .translations(translations)
                .imageUrl("updated-image.jpg")
                .animationUrl("updated-animation.mp4")
                .latitude(43.0)
                .longitude(25.0)
                .isActive(false)
                .build();
    }
}