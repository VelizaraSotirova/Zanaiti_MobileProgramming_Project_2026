package bg.zanaiti.zanaiti_api.service.publics;

import bg.zanaiti.zanaiti_api.dto.Craft.CraftDto;
import bg.zanaiti.zanaiti_api.dto.Craft.CraftTranslationDto;
import bg.zanaiti.zanaiti_api.exceptionHandlers.CraftNotFoundException;
import bg.zanaiti.zanaiti_api.model.Craft;
import bg.zanaiti.zanaiti_api.model.CraftTranslation;
import bg.zanaiti.zanaiti_api.model.Language;
import bg.zanaiti.zanaiti_api.repository.CraftRepository;
import bg.zanaiti.zanaiti_api.repository.CraftTranslationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicCraftServiceTest {

    @Mock
    private CraftRepository craftRepository;

    @Mock
    private CraftTranslationRepository translationRepository;

    @InjectMocks
    private PublicCraftService craftService;

    private Craft activeCraft;
    private Craft inactiveCraft;
    private Language bgLanguage;
    private CraftTranslation bgTranslation;

    @BeforeEach
    void setUp() {
        // Test data setup
        bgLanguage = Language.builder()
                .id(1L)
                .code("bg")
                .name("български")
                .build();

        activeCraft = Craft.builder()
                .id(1L)
                .imageUrl("craft.jpg")
                .animationUrl("animation.mp4")
                .latitude(42.0)
                .longitude(24.0)
                .isActive(true)
                .translations(new ArrayList<>())
                .build();

        inactiveCraft = Craft.builder()
                .id(2L)
                .imageUrl("inactive.jpg")
                .latitude(41.0)
                .longitude(23.0)
                .isActive(false)
                .translations(new ArrayList<>())
                .build();

        bgTranslation = CraftTranslation.builder()
                .id(1L)
                .craft(activeCraft)
                .language(bgLanguage)
                .name("Грънчарство")
                .description("Описание на български")
                .historicalFacts("Исторически факти")
                .makingProcess("Процес на изработка")
                .build();

        activeCraft.getTranslations().add(bgTranslation);
    }
    // ============= getAllCrafts() TESTS =============

    @Test

    void getAllCrafts_ShouldReturnListOfActiveCrafts_WhenActiveCraftsExist() {
        // Arrange
        List<Craft> crafts = List.of(activeCraft);
        when(craftRepository.findByIsActiveTrue()).thenReturn(crafts);

        // Act
        List<CraftDto> result = craftService.getAllCrafts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
        assertTrue(result.getFirst().getIsActive());
        verify(craftRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void getAllCrafts_ShouldReturnEmptyList_WhenNoActiveCrafts() {
        // Arrange
        when(craftRepository.findByIsActiveTrue()).thenReturn(new ArrayList<>());

        // Act
        List<CraftDto> result = craftService.getAllCrafts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(craftRepository, times(1)).findByIsActiveTrue();
    }

    // ============= getCraftById() TESTS =============

    @Test
    void getCraftById_ShouldReturnCraft_WhenCraftExistsAndIsActive() {
        // Arrange
        when(craftRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(activeCraft));

        // Act
        CraftDto result = craftService.getCraftById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("craft.jpg", result.getImageUrl());
        assertEquals(42.0, result.getLatitude());
        assertEquals(24.0, result.getLongitude());
        assertTrue(result.getIsActive());
        verify(craftRepository, times(1)).findByIdAndIsActiveTrue(1L);
    }

    @Test
    void getCraftById_ShouldThrowCraftNotFoundException_WhenCraftDoesNotExist() {
        // Arrange
        when(craftRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CraftNotFoundException exception = assertThrows(CraftNotFoundException.class,
                () -> craftService.getCraftById(999L));

        assertEquals("Craft not found or not active", exception.getMessage());
        verify(craftRepository, times(1)).findByIdAndIsActiveTrue(999L);
    }

    @Test
    void getCraftById_ShouldThrowCraftNotFoundException_WhenCraftExistsButIsInactive() {
        // Arrange
        when(craftRepository.findByIdAndIsActiveTrue(2L)).thenReturn(Optional.empty()); // don't return inactive crafts

        // Act & Assert
        CraftNotFoundException exception = assertThrows(CraftNotFoundException.class,
                () -> craftService.getCraftById(2L));

        assertEquals("Craft not found or not active", exception.getMessage());
        verify(craftRepository, times(1)).findByIdAndIsActiveTrue(2L);
    }

    // ============= getCraftByIdAndLanguage() TESTS =============

    @Test
    void getCraftByIdAndLanguage_ShouldReturnCraftWithTranslation_WhenTranslationExists() {
        // Arrange
        when(craftRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(activeCraft));
        when(translationRepository.findByCraftIdAndLanguageCode(1L, "bg"))
                .thenReturn(Optional.of(bgTranslation));

        // Act
        CraftDto result = craftService.getCraftByIdAndLanguage(1L, "bg");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());

        // Check if the translation is added
        assertNotNull(result.getTranslations());
        assertTrue(result.getTranslations().containsKey("bg"));

        CraftTranslationDto translationDto = result.getTranslations().get("bg");
        assertEquals("Грънчарство", translationDto.getName());
        assertEquals("Описание на български", translationDto.getDescription());
        assertEquals("Исторически факти", translationDto.getHistoricalFacts());
        assertEquals("Процес на изработка", translationDto.getMakingProcess());

        verify(craftRepository, times(1)).findByIdAndIsActiveTrue(1L);
        verify(translationRepository, times(1)).findByCraftIdAndLanguageCode(1L, "bg");
    }

    @Test
    void getCraftByIdAndLanguage_ShouldReturnCraftWithoutTranslation_WhenTranslationDoesNotExist() {
        // Arrange
        when(craftRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(activeCraft));
        when(translationRepository.findByCraftIdAndLanguageCode(1L, "de"))
                .thenReturn(Optional.empty());

        // Act
        CraftDto result = craftService.getCraftByIdAndLanguage(1L, "de");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.getTranslations() == null || result.getTranslations().isEmpty());

        verify(craftRepository, times(1)).findByIdAndIsActiveTrue(1L);
        verify(translationRepository, times(1)).findByCraftIdAndLanguageCode(1L, "de");
    }

    @Test
    void getCraftByIdAndLanguage_ShouldThrowException_WhenCraftNotFound() {
        // Arrange
        when(craftRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CraftNotFoundException exception = assertThrows(CraftNotFoundException.class,
                () -> craftService.getCraftByIdAndLanguage(999L, "bg"));

        assertEquals("Craft not found or not active", exception.getMessage());
        verify(craftRepository, times(1)).findByIdAndIsActiveTrue(999L);
        verify(translationRepository, never()).findByCraftIdAndLanguageCode(anyLong(), anyString());
    }

    // ============= getCraftsInRadius() TESTS =============

    @Test
    void getCraftsInRadius_ShouldReturnCrafts_WhenCraftsExistInRadius() {
        // Arrange
        List<Craft> craftsInRadius = List.of(activeCraft);
        when(craftRepository.findCraftsWithinRadius(42.0, 24.0, 10.0))
                .thenReturn(craftsInRadius);

        // Act
        List<CraftDto> result = craftService.getCraftsInRadius(42.0, 24.0, 10.0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(craftRepository, times(1)).findCraftsWithinRadius(42.0, 24.0, 10.0);
    }

    @Test
    void getCraftsInRadius_ShouldReturnEmptyList_WhenNoCraftsInRadius() {
        // Arrange
        when(craftRepository.findCraftsWithinRadius(42.0, 24.0, 1.0))
                .thenReturn(new ArrayList<>());

        // Act
        List<CraftDto> result = craftService.getCraftsInRadius(42.0, 24.0, 1.0);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(craftRepository, times(1)).findCraftsWithinRadius(42.0, 24.0, 1.0);
    }

    @Test
    void getCraftsInRadius_ShouldNotReturnInactiveCrafts() {
        // Arrange
        List<Craft> activeCrafts = List.of(activeCraft); // inactiveCraft не се връща
        when(craftRepository.findCraftsWithinRadius(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(activeCrafts);

        // Act
        List<CraftDto> result = craftService.getCraftsInRadius(42.0, 24.0, 50.0);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertTrue(result.get(0).getIsActive());

        // Check that inactive craft is not on the list
        assertFalse(result.stream().anyMatch(c -> !c.getIsActive()));
    }

    // ============= BORDER CASES CHECK =============
    @Test
    void getCraftById_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(CraftNotFoundException.class,
                () -> craftService.getCraftById(null));
    }

    @Test
    void getCraftsInRadius_WithZeroRadius_ShouldReturnCraftsAtExactPoint() {
        // Arrange
        when(craftRepository.findCraftsWithinRadius(42.0, 24.0, 0.0))
                .thenReturn(List.of(activeCraft));

        // Act
        List<CraftDto> result = craftService.getCraftsInRadius(42.0, 24.0, 0.0);

        // Assert
        assertEquals(1, result.size());
        verify(craftRepository, times(1)).findCraftsWithinRadius(42.0, 24.0, 0.0);
    }

    @Test
    void getCraftsInRadius_WithNegativeRadius_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> craftService.getCraftsInRadius(42.0, 24.0, -10.0));
    }

    @Test
    void getCraftsInRadius_WithInvalidLatitude_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> craftService.getCraftsInRadius(100.0, 24.0, 10.0));
    }

    @Test
    void getCraftsInRadius_WithInvalidLongitude_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> craftService.getCraftsInRadius(42.0, 200.0, 10.0));
    }
}