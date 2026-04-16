package bg.zanaiti.zanaiti_api.service.user;

import bg.zanaiti.zanaiti_api.dto.Leaderboard.LeaderboardEntryDto;
import bg.zanaiti.zanaiti_api.dto.UserProgressDto.UserProgressDto;
import bg.zanaiti.zanaiti_api.model.*;
import bg.zanaiti.zanaiti_api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProgressServiceTest {

    @Mock
    private UserProgressRepository progressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CraftRepository craftRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private PointsHistoryRepository pointsHistoryRepository;

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @InjectMocks
    private UserProgressService progressService;

    private User testUser;
    private Craft testCraft;
    private Language bgLanguage;
    private UserProgress testProgress;
    private List<QuizQuestion> quizQuestions;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .totalPoints(0)
                .build();

        testCraft = Craft.builder()
                .id(1L)
                .isActive(true)
                .translations(new ArrayList<>())
                .build();

        // Craft name translation
        Language bgLang = Language.builder()
                .code("bg")
                .name("български")
                .build();

        CraftTranslation translation = CraftTranslation.builder()
                .craft(testCraft)
                .language(bgLang)
                .name("Грънчарство")
                .description("Описание...")
                .build();

        testCraft.getTranslations().add(translation);

        bgLanguage = Language.builder()
                .id(1L)
                .code("bg")
                .name("български")
                .build();

        testProgress = UserProgress.builder()
                .id(1L)
                .user(testUser)
                .craft(testCraft)
                .pointsEarned(0)
                .quizCompleted(false)
                .attemptCount(0)
                .lastInteractionDate(LocalDateTime.now())
                .build();

        QuizQuestion question1 = QuizQuestion.builder()
                .id(1L)
                .craft(testCraft)
                .pointsReward(10)
                .isActive(true)
                .build();

        QuizQuestion question2 = QuizQuestion.builder()
                .id(2L)
                .craft(testCraft)
                .pointsReward(10)
                .isActive(true)
                .build();

        quizQuestions = List.of(question1, question2);
    }

    // ============= getUserProgress() TESTS =============

    @Test
    void getUserProgress_ShouldReturnList_WhenUserHasProgress() {
        // Arrange
        List<UserProgress> progresses = List.of(testProgress);
        when(progressRepository.findByUserId(1L)).thenReturn(progresses);

        // Act
        List<UserProgressDto> result = progressService.getUserProgress(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
        assertEquals(1L, result.getFirst().getUserId());
        assertEquals(1L, result.getFirst().getCraftId());
        assertEquals("Грънчарство", result.getFirst().getCraftName());

        verify(progressRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getUserProgress_ShouldReturnEmptyList_WhenUserHasNoProgress() {
        // Arrange
        when(progressRepository.findByUserId(1L)).thenReturn(new ArrayList<>());

        // Act
        List<UserProgressDto> result = progressService.getUserProgress(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(progressRepository, times(1)).findByUserId(1L);
    }

    // ============= getProgressForCraft() TESTS =============

    @Test
    void getProgressForCraft_ShouldReturnProgress_WhenExists() {
        // Arrange
        when(progressRepository.findByUserIdAndCraftId(1L, 1L))
                .thenReturn(Optional.of(testProgress));

        // Act
        UserProgressDto result = progressService.getProgressForCraft(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(progressRepository, times(1)).findByUserIdAndCraftId(1L, 1L);
    }

    @Test
    void getProgressForCraft_ShouldReturnNull_WhenNotExists() {
        // Arrange
        when(progressRepository.findByUserIdAndCraftId(1L, 1L))
                .thenReturn(Optional.empty());

        // Act
        UserProgressDto result = progressService.getProgressForCraft(1L, 1L);

        // Assert
        assertNull(result);
        verify(progressRepository, times(1)).findByUserIdAndCraftId(1L, 1L);
    }

    // ============= recordVisit() TESTS =============

    @Test
    void recordVisit_ShouldCreateNewProgress_WhenFirstVisit() {
        // Arrange
        when(progressRepository.findByUserIdAndCraftId(1L, 1L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(craftRepository.findById(1L)).thenReturn(Optional.of(testCraft));
        when(progressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserProgressDto result = progressService.recordVisit(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(1L, result.getCraftId());
        assertEquals(0, result.getPointsEarned());
        assertEquals(1, result.getAttemptCount()); // attemptCount = 1
        assertFalse(result.isQuizCompleted());

        verify(progressRepository, times(1)).findByUserIdAndCraftId(1L, 1L);
        verify(userRepository, times(1)).findById(1L);
        verify(craftRepository, times(1)).findById(1L);
        verify(progressRepository, times(1)).save(any(UserProgress.class));
    }

    @Test
    void recordVisit_ShouldIncrementAttemptCount_WhenExistingVisit() {
        // Arrange
        testProgress.setAttemptCount(2);
        when(progressRepository.findByUserIdAndCraftId(1L, 1L))
                .thenReturn(Optional.of(testProgress));
        when(progressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserProgressDto result = progressService.recordVisit(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getAttemptCount()); // attemptCount = 2+1
        verify(progressRepository, times(1)).findByUserIdAndCraftId(1L, 1L);
        verify(progressRepository, times(1)).save(any(UserProgress.class));
        verify(userRepository, never()).findById(anyLong());
        verify(craftRepository, never()).findById(anyLong());
    }

    @Test
    void recordVisit_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(progressRepository.findByUserIdAndCraftId(99L, 1L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> progressService.recordVisit(99L, 1L));

        assertEquals("User not found", exception.getMessage());
        verify(progressRepository, times(1)).findByUserIdAndCraftId(99L, 1L);
        verify(userRepository, times(1)).findById(99L);
        verify(craftRepository, never()).findById(anyLong());
    }

    @Test
    void recordVisit_ShouldThrowException_WhenCraftNotFound() {
        // Arrange
        when(progressRepository.findByUserIdAndCraftId(1L, 99L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(craftRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> progressService.recordVisit(1L, 99L));

        assertEquals("Craft not found", exception.getMessage());
        verify(progressRepository, times(1)).findByUserIdAndCraftId(1L, 99L);
        verify(userRepository, times(1)).findById(1L);
        verify(craftRepository, times(1)).findById(99L);
    }

    // ============= completeQuiz() TESTS =============

    @Test
    void completeQuiz_ShouldAddPoints_WhenFirstTime() {
        // Arrange
        when(progressRepository.findByUserIdAndCraftId(1L, 1L))
                .thenReturn(Optional.of(testProgress));
        when(quizQuestionRepository.findByCraftIdAndIsActiveTrue(1L))
                .thenReturn(quizQuestions);
        when(languageRepository.findByCode("bg")).thenReturn(Optional.of(bgLanguage));
        when(progressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserProgressDto result = progressService.completeQuiz(1L, 1L, 2, "bg");

        // Assert
        assertNotNull(result);
        assertEquals(20, result.getPointsEarned()); // 2 правилни × 10 точки = 20
        assertTrue(result.isQuizCompleted());
        assertEquals(2, result.getQuizScore());
        assertEquals("bg", result.getLanguageCode());

        verify(userRepository, times(1)).save(any(User.class));
        verify(pointsHistoryRepository, times(1)).save(any(PointsHistory.class));
    }

    @Test
    void completeQuiz_ShouldNotAddPoints_WhenAlreadyCompleted() {
        // Arrange
        testProgress.setQuizCompleted(true);
        testProgress.setPointsEarned(20);
        when(progressRepository.findByUserIdAndCraftId(1L, 1L))
                .thenReturn(Optional.of(testProgress));

        // Mock returns the same object when save()
        when(progressRepository.save(any(UserProgress.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        UserProgressDto result = progressService.completeQuiz(1L, 1L, 2, "bg");

        // Assert
        assertNotNull(result);
        assertEquals(20, result.getPointsEarned()); // same points
        assertTrue(result.isQuizCompleted());

        verify(quizQuestionRepository, never()).findByCraftIdAndIsActiveTrue(anyLong());
        verify(userRepository, never()).save(any(User.class));
        verify(pointsHistoryRepository, never()).save(any(PointsHistory.class));
        verify(progressRepository, times(1)).save(any(UserProgress.class));
    }

    @Test
    void completeQuiz_ShouldCalculatePointsCorrectly_WithDifferentRewards() {
        // Arrange
        QuizQuestion q1 = QuizQuestion.builder().pointsReward(10).build();
        QuizQuestion q2 = QuizQuestion.builder().pointsReward(15).build();
        List<QuizQuestion> mixedQuestions = List.of(q1, q2);

        when(progressRepository.findByUserIdAndCraftId(1L, 1L))
                .thenReturn(Optional.of(testProgress));
        when(quizQuestionRepository.findByCraftIdAndIsActiveTrue(1L))
                .thenReturn(mixedQuestions);
        when(languageRepository.findByCode("bg")).thenReturn(Optional.of(bgLanguage));
        when(progressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        // Act (2 correct answers, but questions has different pointsReward)
        // Забележка: в текущата имплементация се използва първият въпрос за изчисление
        UserProgressDto result = progressService.completeQuiz(1L, 1L, 2, "bg");

        // Assert
        assertNotNull(result);
        assertEquals(20, result.getPointsEarned()); // 2 × 10 (първият въпрос)
    }

    @Test
    void completeQuiz_ShouldUseDefaultPoints_WhenNoQuestions() {
        // Arrange
        when(progressRepository.findByUserIdAndCraftId(1L, 1L))
                .thenReturn(Optional.of(testProgress));
        when(quizQuestionRepository.findByCraftIdAndIsActiveTrue(1L))
                .thenReturn(new ArrayList<>()); // empty list
        when(languageRepository.findByCode("bg")).thenReturn(Optional.of(bgLanguage));
        when(progressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserProgressDto result = progressService.completeQuiz(1L, 1L, 2, "bg");

        // Assert
        assertNotNull(result);
        assertEquals(20, result.getPointsEarned()); // 2 × 10 (default)
    }

    // ============= getLeaderboard() TESTS =============

    @Test
    void getLeaderboard_ShouldReturnList_WhenPointsExist() {
        // Arrange
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{1L, "admin", 150});
        mockResults.add(new Object[]{2L, "ivan", 50});

        when(progressRepository.getLeaderboard()).thenReturn(mockResults);

        // Act
        List<LeaderboardEntryDto> result = progressService.getLeaderboard();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getUserId());
        assertEquals("admin", result.get(0).getUsername());
        assertEquals(150, result.get(0).getTotalPoints());

        assertEquals(2L, result.get(1).getUserId());
        assertEquals("ivan", result.get(1).getUsername());
        assertEquals(50, result.get(1).getTotalPoints());

        verify(progressRepository, times(1)).getLeaderboard();
    }

    @Test
    void getLeaderboard_ShouldReturnEmptyList_WhenNoPoints() {
        // Arrange
        when(progressRepository.getLeaderboard()).thenReturn(new ArrayList<>());

        // Act
        List<LeaderboardEntryDto> result = progressService.getLeaderboard();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(progressRepository, times(1)).getLeaderboard();
    }

    // ============= EXCEPTION TESTS =============

    @Test
    void completeQuiz_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(progressRepository.findByUserIdAndCraftId(99L, 1L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> progressService.completeQuiz(99L, 1L, 2, "bg"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void completeQuiz_ShouldThrowException_WhenCraftNotFound() {
        // Arrange
        when(progressRepository.findByUserIdAndCraftId(1L, 99L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(craftRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> progressService.completeQuiz(1L, 99L, 2, "bg"));

        assertEquals("Craft not found", exception.getMessage());
    }

    @Test
    void completeQuiz_WithInvalidLanguage_ShouldProceedWithNullLanguage() {
        // Arrange
        when(progressRepository.findByUserIdAndCraftId(1L, 1L))
                .thenReturn(Optional.of(testProgress));
        when(quizQuestionRepository.findByCraftIdAndIsActiveTrue(1L))
                .thenReturn(quizQuestions);
        when(languageRepository.findByCode("xx")).thenReturn(Optional.empty());
        when(progressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserProgressDto result = progressService.completeQuiz(1L, 1L, 2, "xx");

        // Assert
        assertNotNull(result);
        assertEquals(20, result.getPointsEarned());
        assertNull(result.getLanguageCode());
    }
}