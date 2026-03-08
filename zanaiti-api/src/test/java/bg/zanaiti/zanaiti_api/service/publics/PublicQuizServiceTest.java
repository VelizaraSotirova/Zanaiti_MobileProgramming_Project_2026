package bg.zanaiti.zanaiti_api.service.publics;

import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionDto;
import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionTranslationDto;
import bg.zanaiti.zanaiti_api.model.Craft;
import bg.zanaiti.zanaiti_api.model.Language;
import bg.zanaiti.zanaiti_api.model.QuizQuestion;
import bg.zanaiti.zanaiti_api.model.QuizQuestionTranslation;
import bg.zanaiti.zanaiti_api.repository.QuizQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicQuizServiceTest {

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @InjectMocks
    private PublicQuizService quizService;

    private Craft testCraft;
    private Language bgLanguage;
    private QuizQuestion question1;
    private QuizQuestion question2;
    private QuizQuestionTranslation translation1Bg;
    private QuizQuestionTranslation translation1En;

    @BeforeEach
    void setUp() {
        testCraft = Craft.builder().id(1L).build();

        bgLanguage = Language.builder()
                .id(1L)
                .code("bg")
                .name("български")
                .build();

        Language enLanguage = Language.builder()
                .id(2L)
                .code("en")
                .name("English")
                .build();

        // Question 1
        question1 = QuizQuestion.builder()
                .id(1L)
                .craft(testCraft)
                .correctOptionIndex(1) // B
                .pointsReward(10)
                .isActive(true)
                .translations(new ArrayList<>())
                .build();

        translation1Bg = QuizQuestionTranslation.builder()
                .id(1L)
                .quizQuestion(question1)
                .language(bgLanguage)
                .questionText("Какъв материал се използва за грънчарство?")
                .optionA("Дърво")
                .optionB("Глина")
                .optionC("Метал")
                .optionD("Камък")
                .build();

        translation1En = QuizQuestionTranslation.builder()
                .id(2L)
                .quizQuestion(question1)
                .language(enLanguage)
                .questionText("What material is used for pottery?")
                .optionA("Wood")
                .optionB("Clay")
                .optionC("Metal")
                .optionD("Stone")
                .build();

        question1.getTranslations().add(translation1Bg);
        question1.getTranslations().add(translation1En);

        // Question 2
        question2 = QuizQuestion.builder()
                .id(2L)
                .craft(testCraft)
                .correctOptionIndex(0) // A
                .pointsReward(10)
                .isActive(true)
                .translations(new ArrayList<>())
                .build();

        QuizQuestionTranslation translation2Bg = QuizQuestionTranslation.builder()
                .id(3L)
                .quizQuestion(question2)
                .language(bgLanguage)
                .questionText("Как се оформя съд от глина?")
                .optionA("С грънчарско колело")
                .optionB("Чрез шприцоване")
                .optionC("Чрез рязане")
                .optionD("Чрез лепене")
                .build();

        question2.getTranslations().add(translation2Bg);
    }

    // ============= getQuestionsByCraftAndLanguage() TESTS =============

    @Test
    void getQuestionsByCraftAndLanguage_ShouldReturnQuestions_WhenCraftExistsAndHasQuestions() {
        // Arrange
        List<QuizQuestion> questions = List.of(question1, question2);
        when(quizQuestionRepository.findQuizQuestionsWithTranslations(1L, "bg"))
                .thenReturn(questions);

        // Act
        List<QuizQuestionDto> result = quizService.getQuestionsByCraftAndLanguage(1L, "bg");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // First question check
        QuizQuestionDto firstQuestion = result.getFirst();
        assertEquals(1L, firstQuestion.getId());
        assertEquals(1L, firstQuestion.getCraftId());
        assertEquals(1, firstQuestion.getCorrectOptionIndex());
        assertEquals(10, firstQuestion.getPointsReward());
        assertTrue(firstQuestion.isActive());

        // Translation check
        Map<String, QuizQuestionTranslationDto> translations = firstQuestion.getTranslations();
        assertTrue(translations.containsKey("bg"));
        QuizQuestionTranslationDto bgTranslation = translations.get("bg");
        assertEquals("Какъв материал се използва за грънчарство?", bgTranslation.getQuestionText());
        assertEquals("Дърво", bgTranslation.getOptionA());
        assertEquals("Глина", bgTranslation.getOptionB());
        assertEquals("Метал", bgTranslation.getOptionC());
        assertEquals("Камък", bgTranslation.getOptionD());

        verify(quizQuestionRepository, times(1)).findQuizQuestionsWithTranslations(1L, "bg");
    }

    @Test
    void getQuestionsByCraftAndLanguage_ShouldReturnQuestions_WhenCraftExistsAndHasQuestionsInEnglish() {
        // Arrange
        List<QuizQuestion> questions = List.of(question1);
        when(quizQuestionRepository.findQuizQuestionsWithTranslations(1L, "en"))
                .thenReturn(questions);

        // Act
        List<QuizQuestionDto> result = quizService.getQuestionsByCraftAndLanguage(1L, "en");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        QuizQuestionDto question = result.getFirst();
        Map<String, QuizQuestionTranslationDto> translations = question.getTranslations();
        assertTrue(translations.containsKey("en"));
        QuizQuestionTranslationDto enTranslation = translations.get("en");
        assertEquals("What material is used for pottery?", enTranslation.getQuestionText());
        assertEquals("Wood", enTranslation.getOptionA());
        assertEquals("Clay", enTranslation.getOptionB());
        assertEquals("Metal", enTranslation.getOptionC());
        assertEquals("Stone", enTranslation.getOptionD());

        verify(quizQuestionRepository, times(1)).findQuizQuestionsWithTranslations(1L, "en");
    }

    @Test
    void getQuestionsByCraftAndLanguage_ShouldReturnEmptyList_WhenCraftHasNoQuestions() {
        // Arrange
        when(quizQuestionRepository.findQuizQuestionsWithTranslations(1L, "bg"))
                .thenReturn(new ArrayList<>());

        // Act
        List<QuizQuestionDto> result = quizService.getQuestionsByCraftAndLanguage(1L, "bg");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(quizQuestionRepository, times(1)).findQuizQuestionsWithTranslations(1L, "bg");
    }

    @Test
    void getQuestionsByCraftAndLanguage_ShouldReturnQuestionsWithoutTranslation_WhenLanguageNotFound() {
        // Arrange
        List<QuizQuestion> questions = List.of(question1);
        when(quizQuestionRepository.findQuizQuestionsWithTranslations(1L, "de"))
                .thenReturn(questions);

        // Act
        List<QuizQuestionDto> result = quizService.getQuestionsByCraftAndLanguage(1L, "de");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        // Returns the question but without translation (empty Map)
        assertTrue(result.getFirst().getTranslations().isEmpty());
        verify(quizQuestionRepository, times(1)).findQuizQuestionsWithTranslations(1L, "de");
    }

    @Test
    void getQuestionsByCraftAndLanguage_ShouldThrowException_WhenRepositoryThrowsException() {
        // Arrange
        when(quizQuestionRepository.findQuizQuestionsWithTranslations(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> quizService.getQuestionsByCraftAndLanguage(1L, "bg"));

        assertEquals("Database error", exception.getMessage());
        verify(quizQuestionRepository, times(1)).findQuizQuestionsWithTranslations(1L, "bg");
    }

    // ============= checkAnswer() TESTS =============

    @Test
    void checkAnswer_ShouldReturnTrue_WhenAnswerIsCorrect() {
        // Arrange
        when(quizQuestionRepository.findById(1L)).thenReturn(Optional.of(question1));

        // Act
        boolean result = quizService.checkAnswer(1L, 1); // correctOptionIndex = 1

        // Assert
        assertTrue(result);
        verify(quizQuestionRepository, times(1)).findById(1L);
    }

    @Test
    void checkAnswer_ShouldReturnFalse_WhenAnswerIsIncorrect() {
        // Arrange
        when(quizQuestionRepository.findById(1L)).thenReturn(Optional.of(question1));

        // Act
        boolean result = quizService.checkAnswer(1L, 0); // chosen option A, correct one - B (1)

        // Assert
        assertFalse(result);
        verify(quizQuestionRepository, times(1)).findById(1L);
    }

    @Test
    void checkAnswer_ShouldThrowException_WhenQuestionNotFound() {
        // Arrange
        when(quizQuestionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> quizService.checkAnswer(999L, 1));

        assertTrue(exception.getMessage().contains("QuizQuestion not found"));
        verify(quizQuestionRepository, times(1)).findById(999L);
    }

    @Test
    void checkAnswer_ShouldThrowException_WhenQuestionIdIsNull() {
        // Act & Assert
        assertThrows(Exception.class,
                () -> quizService.checkAnswer(null, 1));
    }

    @Test
    void checkAnswer_WithInvalidOptionIndex_ShouldReturnFalse() {
        // Arrange
        when(quizQuestionRepository.findById(1L)).thenReturn(Optional.of(question1));

        // Act
        boolean result = quizService.checkAnswer(1L, 99); // non-existing option

        // Assert
        assertFalse(result);
        verify(quizQuestionRepository, times(1)).findById(1L);
    }
}