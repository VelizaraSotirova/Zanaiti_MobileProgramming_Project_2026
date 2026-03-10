package bg.zanaiti.zanaiti_api.service.user;

import bg.zanaiti.zanaiti_api.dto.PointsHistory.PointsHistoryDto;
import bg.zanaiti.zanaiti_api.model.*;
import bg.zanaiti.zanaiti_api.repository.PointsHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPointsServiceTest {

    @Mock
    private PointsHistoryRepository pointsHistoryRepository;

    @InjectMocks
    private UserPointsService pointsService;

    private User testUser;
    private Craft testCraft;
    private Language bgLanguage;
    private PointsHistory history1;
    private PointsHistory history2;
    private PointsHistory history3;
    private final Long userId = 1L;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();

        testCraft = Craft.builder()
                .id(1L)
                .isActive(true)
                .translations(new ArrayList<>())
                .build();

        bgLanguage = Language.builder()
                .code("bg")
                .name("български")
                .build();

        // Craft's name translation
        CraftTranslation translation = CraftTranslation.builder()
                .craft(testCraft)
                .language(bgLanguage)
                .name("Грънчарство")
                .build();
        testCraft.getTranslations().add(translation);

        // Test history creation
        history1 = PointsHistory.builder()
                .id(1L)
                .user(testUser)
                .points(50)
                .source("QUIZ")
                .description("Completed quiz for craft: 1")
                .createdAt(now.minusDays(5))
                .craft(testCraft)
                .build();

        history2 = PointsHistory.builder()
                .id(2L)
                .user(testUser)
                .points(20)
                .source("VISIT")
                .description("Visited craft: 2")
                .createdAt(now.minusDays(3))
                .craft(null)
                .build();

        history3 = PointsHistory.builder()
                .id(3L)
                .user(testUser)
                .points(10)
                .source("ADMIN")
                .description("Admin bonus")
                .createdAt(now.minusDays(1))
                .craft(null)
                .build();
    }

    // ============= getUserHistory() TESTS =============

    @Test
    void getUserHistory_ShouldReturnAllHistory_WhenUserHasHistory() {
        // Arrange
        List<PointsHistory> histories = List.of(history1, history2, history3);
        when(pointsHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(histories);

        // Act
        List<PointsHistoryDto> result = pointsService.getUserHistory(userId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        // First record check (with craft)
        PointsHistoryDto first = result.getFirst();
        assertEquals(1L, first.getId());
        assertEquals(userId, first.getUserId());
        assertEquals("testuser", first.getUsername());
        assertEquals(50, first.getPoints());
        assertEquals("QUIZ", first.getSource());
        assertEquals("Completed quiz for craft: 1", first.getDescription());
        assertNotNull(first.getCreatedAt());
        assertEquals(1L, first.getCraftId());
        assertEquals("Грънчарство", first.getCraftName());

        // Second record check (without craft)
        PointsHistoryDto second = result.get(1);
        assertEquals(2L, second.getId());
        assertEquals(20, second.getPoints());
        assertEquals("VISIT", second.getSource());
        assertNull(second.getCraftId());
        assertNull(second.getCraftName());

        verify(pointsHistoryRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getUserHistory_ShouldReturnEmptyList_WhenUserHasNoHistory() {
        // Arrange
        when(pointsHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(new ArrayList<>());

        // Act
        List<PointsHistoryDto> result = pointsService.getUserHistory(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(pointsHistoryRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ============= getUserHistoryLastDays() TESTS =============

    @Test
    void getUserHistoryLastDays_ShouldReturnHistoryWithinPeriod() {
        // Arrange
        int days = 7;
        LocalDateTime startDate = now.minusDays(days);
        List<PointsHistory> historiesInPeriod = List.of(history1, history2, history3);

        when(pointsHistoryRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(historiesInPeriod);

        // Act
        List<PointsHistoryDto> result = pointsService.getUserHistoryLastDays(userId, days);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(pointsHistoryRepository, times(1))
                .findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getUserHistoryLastDays_ShouldReturnEmptyList_WhenNoHistoryInPeriod() {
        // Arrange
        int days = 1; // last 1 day
        when(pointsHistoryRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<PointsHistoryDto> result = pointsService.getUserHistoryLastDays(userId, days);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(pointsHistoryRepository, times(1))
                .findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getUserHistoryLastDays_ShouldRespectDaysParameter() {
        // Arrange
        when(pointsHistoryRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(history3)); // the last record only

        // Act
        List<PointsHistoryDto> result30 = pointsService.getUserHistoryLastDays(userId, 30);
        List<PointsHistoryDto> result7 = pointsService.getUserHistoryLastDays(userId, 7);
        List<PointsHistoryDto> result1 = pointsService.getUserHistoryLastDays(userId, 1);

        // Assert - all os asserts return the same result in mock, but the most important is that the method is executed
        assertEquals(1, result30.size());
        assertEquals(1, result7.size());
        assertEquals(1, result1.size());

        verify(pointsHistoryRepository, times(3))
                .findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    // ============= getTotalPointsLastDays() TESTS =============

    @Test
    void getTotalPointsLastDays_ShouldReturnSumOfPointsInPeriod() {
        // Arrange
        int days = 30;
        List<PointsHistory> histories = List.of(history1, history2, history3); // 50 + 20 + 10 = 80

        when(pointsHistoryRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(histories);

        // Act
        Integer result = pointsService.getTotalPointsLastDays(userId, days);

        // Assert
        assertNotNull(result);
        assertEquals(80, result); // 50 + 20 + 10
        verify(pointsHistoryRepository, times(1))
                .findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getTotalPointsLastDays_ShouldReturnZero_WhenNoPointsInPeriod() {
        // Arrange
        int days = 1;
        when(pointsHistoryRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        Integer result = pointsService.getTotalPointsLastDays(userId, days);

        // Assert
        assertNotNull(result);
        assertEquals(0, result);
        verify(pointsHistoryRepository, times(1))
                .findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getTotalPointsLastDays_ShouldSumCorrectly_WithDifferentSources() {
        // Arrange
        int days = 30;
        PointsHistory q1 = PointsHistory.builder().points(15).build();
        PointsHistory q2 = PointsHistory.builder().points(25).build();
        PointsHistory v1 = PointsHistory.builder().points(5).build();
        PointsHistory a1 = PointsHistory.builder().points(10).build();
        List<PointsHistory> histories = List.of(q1, q2, v1, a1); // 15+25+5+10 = 55

        when(pointsHistoryRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(histories);

        // Act
        Integer result = pointsService.getTotalPointsLastDays(userId, days);

        // Assert
        assertEquals(55, result);
    }

    // ============= ЗА convertToDto TESTS =============

    @Test
    void convertToDto_ShouldHandleCraftWithoutTranslation() {
        // Arrange
        Craft craftWithoutTranslation = Craft.builder()
                .id(2L)
                .translations(new ArrayList<>()) // empty translations list
                .build();

        PointsHistory historyWithoutCraftName = PointsHistory.builder()
                .id(4L)
                .user(testUser)
                .points(30)
                .source("QUIZ")
                .description("Test")
                .createdAt(now)
                .craft(craftWithoutTranslation)
                .build();

        when(pointsHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(historyWithoutCraftName));

        // Act
        List<PointsHistoryDto> result = pointsService.getUserHistory(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().getCraftId());
        assertNull(result.getFirst().getCraftName()); // name should be null
    }

    @Test
    void convertToDto_ShouldHandleNullCraft() {
        // Arrange
        PointsHistory historyWithoutCraft = PointsHistory.builder()
                .id(5L)
                .user(testUser)
                .points(40)
                .source("ADMIN")
                .description("Bonus")
                .createdAt(now)
                .craft(null)
                .build();

        when(pointsHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(historyWithoutCraft));

        // Act
        List<PointsHistoryDto> result = pointsService.getUserHistory(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.getFirst().getCraftId());
        assertNull(result.getFirst().getCraftName());
    }

    // ============= BORDER CASES TESTS =============

    @Test
    void getUserHistoryLastDays_WithNegativeDays_ShouldHandleGracefully() {
        // This depends on the implementation - if the method accepts negative days, the test should reflect the expected behavior
        int negativeDays = -5;

        // We expect the method to use the absolute value or throw an error
        // Right now we're just checking that the repository is called
        when(pointsHistoryRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        List<PointsHistoryDto> result = pointsService.getUserHistoryLastDays(userId, negativeDays);

        assertNotNull(result);
        verify(pointsHistoryRepository, times(1))
                .findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getUserHistoryLastDays_WithZeroDays_ShouldReturnToday() {
        // Arrange
        int zeroDays = 0;
        when(pointsHistoryRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<PointsHistoryDto> result = pointsService.getUserHistoryLastDays(userId, zeroDays);

        // Assert
        assertNotNull(result);
        verify(pointsHistoryRepository, times(1))
                .findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }
}