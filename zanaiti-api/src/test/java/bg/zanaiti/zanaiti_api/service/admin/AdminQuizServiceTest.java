package bg.zanaiti.zanaiti_api.service.admin;

import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionCreateDto;
import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionDto;
import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionTranslationDto;
import bg.zanaiti.zanaiti_api.exceptionHandlers.DuplicateQuestionException;
import bg.zanaiti.zanaiti_api.model.*;
import bg.zanaiti.zanaiti_api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminQuizServiceTest {

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @Mock
    private QuizQuestionTranslationRepository translationRepository;

    @Mock
    private CraftRepository craftRepository;

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private AdminQuizService adminQuizService;

    private Craft testCraft;
    private Language bgLanguage;
    private Language enLanguage;
    private QuizQuestion testQuestion;
    private QuizQuestionTranslation bgTranslation;
    private QuizQuestionTranslation enTranslation;
    private final Long craftId = 1L;
    private final Long questionId = 1L;

    @BeforeEach
    void setUp() {
        testCraft = Craft.builder()
                .id(craftId)
                .isActive(true)
                .translations(new ArrayList<>())
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

        // Craft's name translation adding
        CraftTranslation craftTranslation = CraftTranslation.builder()
                .craft(testCraft)
                .language(bgLanguage)
                .name("Грънчарство")
                .build();

        testCraft.getTranslations().add(craftTranslation);

        testQuestion = QuizQuestion.builder()
                .id(questionId)
                .craft(testCraft)
                .correctOptionIndex(1) // B
                .pointsReward(10)
                .isActive(true)
                .translations(new ArrayList<>())
                .build();

        bgTranslation = QuizQuestionTranslation.builder()
                .id(1L)
                .quizQuestion(testQuestion)
                .language(bgLanguage)
                .questionText("Тестов въпрос на бг?")
                .optionA("Опция А")
                .optionB("Опция Б")
                .optionC("Опция В")
                .optionD("Опция Г")
                .build();

        enTranslation = QuizQuestionTranslation.builder()
                .id(2L)
                .quizQuestion(testQuestion)
                .language(enLanguage)
                .questionText("Test question in en?")
                .optionA("Option A")
                .optionB("Option B")
                .optionC("Option C")
                .optionD("Option D")
                .build();

        testQuestion.getTranslations().add(bgTranslation);
        testQuestion.getTranslations().add(enTranslation);
    }

    // ============= createQuestion() TESTS =============

    @Test
    void createQuestion_ShouldCreateQuestion_WhenDataIsValid() {
        // Arrange
        QuizQuestionCreateDto createDto = createValidCreateDto();

        when(quizQuestionRepository.existsDuplicateQuestion(craftId, "Нов въпрос на бг?", "bg"))
                .thenReturn(false);
        when(craftRepository.findById(craftId)).thenReturn(Optional.of(testCraft));
        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenAnswer(i -> {
            QuizQuestion saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(languageRepository.findByCode("bg")).thenReturn(Optional.of(bgLanguage));
        when(languageRepository.findByCode("en")).thenReturn(Optional.of(enLanguage));
        when(translationRepository.save(any(QuizQuestionTranslation.class)))
                .thenAnswer(i -> i.getArgument(0));

        // Act
        QuizQuestionDto result = adminQuizService.createQuestion(createDto);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(craftId, result.getCraftId());
        assertEquals(2, result.getCorrectOptionIndex());
        assertEquals(15, result.getPointsReward());
        assertTrue(result.isActive());

        // Translations check
        assertNotNull(result.getTranslations());
        assertEquals(2, result.getTranslations().size());
        assertTrue(result.getTranslations().containsKey("bg"));
        assertTrue(result.getTranslations().containsKey("en"));

        QuizQuestionTranslationDto bgTrans = result.getTranslations().get("bg");
        assertEquals("Нов въпрос на бг?", bgTrans.getQuestionText());
        assertEquals("А1", bgTrans.getOptionA());
        assertEquals("А2", bgTrans.getOptionB());

        QuizQuestionTranslationDto enTrans = result.getTranslations().get("en");
        assertEquals("New question in en?", enTrans.getQuestionText());
        assertEquals("A1", enTrans.getOptionA());
        assertEquals("A2", enTrans.getOptionB());

        verify(quizQuestionRepository, times(1)).existsDuplicateQuestion(craftId, "Нов въпрос на бг?", "bg");
        verify(craftRepository, times(1)).findById(craftId);
        verify(quizQuestionRepository, times(1)).save(any(QuizQuestion.class));
        verify(languageRepository, times(1)).findByCode("bg");
        verify(languageRepository, times(1)).findByCode("en");
        verify(translationRepository, times(2)).save(any(QuizQuestionTranslation.class));
    }

    @Test
    void createQuestion_ShouldThrowDuplicateQuestionException_WhenQuestionWithSameTextExists() {
        // Arrange
        QuizQuestionCreateDto createDto = createValidCreateDto();

        when(quizQuestionRepository.existsDuplicateQuestion(craftId, "Нов въпрос на бг?", "bg"))
                .thenReturn(true); // already exists

        // Act & Assert
        DuplicateQuestionException exception = assertThrows(DuplicateQuestionException.class,
                () -> adminQuizService.createQuestion(createDto));

        assertEquals("Question 'Нов въпрос на бг?' already exists for this craft!",
                exception.getMessage());

        verify(quizQuestionRepository, times(1)).existsDuplicateQuestion(craftId, "Нов въпрос на бг?", "bg");
        verify(craftRepository, never()).findById(anyLong());
        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
    }

    @Test
    void createQuestion_ShouldThrowException_WhenCraftNotFound() {
        // Arrange
        QuizQuestionCreateDto createDto = createValidCreateDto();

        when(quizQuestionRepository.existsDuplicateQuestion(craftId, "Нов въпрос на бг?", "bg"))
                .thenReturn(false);
        when(craftRepository.findById(craftId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminQuizService.createQuestion(createDto));

        assertEquals("Craft not found with id: " + craftId, exception.getMessage());
        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
    }

    @Test
    void createQuestion_ShouldThrowException_WhenLanguageNotFound() {
        // Arrange
        QuizQuestionCreateDto createDto = createValidCreateDto();

        when(quizQuestionRepository.existsDuplicateQuestion(craftId, "Нов въпрос на бг?", "bg"))
                .thenReturn(false);
        when(craftRepository.findById(craftId)).thenReturn(Optional.of(testCraft));
        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenAnswer(i -> {
            QuizQuestion saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(languageRepository.findByCode("bg")).thenReturn(Optional.empty()); // language not found

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminQuizService.createQuestion(createDto));

        assertEquals("Language not found: bg", exception.getMessage());
        verify(translationRepository, never()).save(any(QuizQuestionTranslation.class));
    }

    // ============= updateQuestion() TESTS =============

    @Test
    void updateQuestion_ShouldUpdateQuestion_WhenDataIsValid() {
        // Arrange
        QuizQuestionCreateDto updateDto = createValidUpdateDto();

        when(quizQuestionRepository.findById(questionId)).thenReturn(Optional.of(testQuestion));
        lenient().when(craftRepository.findById(craftId)).thenReturn(Optional.of(testCraft));
        when(languageRepository.findByCode("bg")).thenReturn(Optional.of(bgLanguage));
        when(languageRepository.findByCode("en")).thenReturn(Optional.of(enLanguage));
        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenReturn(testQuestion);

        // Act
        QuizQuestionDto result = adminQuizService.updateQuestion(questionId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(questionId, result.getId());
        assertEquals(craftId, result.getCraftId());
        assertEquals(2, result.getCorrectOptionIndex()); // updated
        assertEquals(20, result.getPointsReward()); // updated

        // Check if old translations are deleted and the new ones are added
        verify(translationRepository, times(1)).deleteAll(testQuestion.getTranslations());
        verify(translationRepository, times(2)).save(any(QuizQuestionTranslation.class));
        verify(quizQuestionRepository, times(1)).save(any(QuizQuestion.class));
    }

    @Test
    void updateQuestion_ShouldUpdateOnlyCorrectOptionIndex_WhenOnlyThatProvided() {
        // Arrange - only correctOptionIndex changes
        QuizQuestionCreateDto updateDto = QuizQuestionCreateDto.builder()
                .craftId(craftId)
                .correctOptionIndex(3) // D
                .translations(new HashMap<>()) // empty translations – won't update
                .build();

        when(quizQuestionRepository.findById(questionId)).thenReturn(Optional.of(testQuestion));

        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenAnswer(i -> i.getArgument(0));
        // Act
        QuizQuestionDto result = adminQuizService.updateQuestion(questionId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(3, testQuestion.getCorrectOptionIndex());
        assertEquals(10, testQuestion.getPointsReward()); // unchanged
        verify(translationRepository, never()).deleteAll(any());
        verify(translationRepository, never()).save(any(QuizQuestionTranslation.class));
        verify(quizQuestionRepository, times(1)).save(any(QuizQuestion.class));
    }

    @Test
    void updateQuestion_ShouldUpdateOnlyPointsReward_WhenOnlyThatProvided() {
        // Arrange - only pointsReward change
        QuizQuestionCreateDto updateDto = QuizQuestionCreateDto.builder()
                .craftId(craftId)
                .pointsReward(25)
                .translations(new HashMap<>())
                .build();

        when(quizQuestionRepository.findById(questionId)).thenReturn(Optional.of(testQuestion));

        // Mock returns the same object when save()
        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenAnswer(i -> i.getArgument(0));
        // Act
        QuizQuestionDto result = adminQuizService.updateQuestion(questionId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, testQuestion.getCorrectOptionIndex()); // unchanged
        assertEquals(25, testQuestion.getPointsReward()); // updated
        verify(translationRepository, never()).deleteAll(any());
        verify(quizQuestionRepository, times(1)).save(any(QuizQuestion.class));
    }

    @Test
    void updateQuestion_ShouldUpdateCraft_WhenNewCraftProvided() {
        // Arrange
        Long newCraftId = 2L;
        Craft newCraft = Craft.builder().id(newCraftId).build();

        QuizQuestionCreateDto updateDto = QuizQuestionCreateDto.builder()
                .craftId(newCraftId)
                .translations(new HashMap<>())
                .build();

        when(quizQuestionRepository.findById(questionId)).thenReturn(Optional.of(testQuestion));
        when(craftRepository.findById(newCraftId)).thenReturn(Optional.of(newCraft));


        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenAnswer(i -> i.getArgument(0));
        // Act
        QuizQuestionDto result = adminQuizService.updateQuestion(questionId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(newCraftId, testQuestion.getCraft().getId());
        verify(craftRepository, times(1)).findById(newCraftId);
        verify(quizQuestionRepository, times(1)).save(any(QuizQuestion.class));
    }

    @Test
    void updateQuestion_ShouldThrowException_WhenQuestionNotFound() {
        // Arrange
        QuizQuestionCreateDto updateDto = createValidUpdateDto();

        when(quizQuestionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminQuizService.updateQuestion(999L, updateDto));

        assertEquals("Question not found with id: 999", exception.getMessage());
        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
    }

    @Test
    void updateQuestion_ShouldThrowException_WhenCraftNotFound() {
        // Arrange
        QuizQuestionCreateDto updateDto = QuizQuestionCreateDto.builder()
                .craftId(999L)
                .translations(new HashMap<>())
                .build();

        when(quizQuestionRepository.findById(questionId)).thenReturn(Optional.of(testQuestion));
        when(craftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminQuizService.updateQuestion(questionId, updateDto));

        assertEquals("Craft not found", exception.getMessage());
        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
    }

    @Test
    void updateQuestion_ShouldThrowException_WhenLanguageNotFound() {
        // Arrange
        QuizQuestionCreateDto updateDto = createValidUpdateDto();

        when(quizQuestionRepository.findById(questionId)).thenReturn(Optional.of(testQuestion));
        lenient().when(craftRepository.findById(craftId)).thenReturn(Optional.of(testCraft));
        when(languageRepository.findByCode("bg")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminQuizService.updateQuestion(questionId, updateDto));

        assertEquals("Language not found: bg", exception.getMessage());
        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
    }

    // ============= deleteQuestion() TESTS =============

    @Test
    void deleteQuestion_ShouldDeactivateQuestion_WhenQuestionExists() {
        // Arrange
        when(quizQuestionRepository.findById(questionId)).thenReturn(Optional.of(testQuestion));
        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenReturn(testQuestion);

        // Act
        adminQuizService.deleteQuestion(questionId);

        // Assert
        assertFalse(testQuestion.isActive()); // not active now
        verify(quizQuestionRepository, times(1)).findById(questionId);
        verify(quizQuestionRepository, times(1)).save(testQuestion);
    }

    @Test
    void deleteQuestion_ShouldThrowException_WhenQuestionNotFound() {
        // Arrange
        when(quizQuestionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminQuizService.deleteQuestion(999L));

        assertEquals("Question not found with id: 999", exception.getMessage());
        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
    }



    private QuizQuestionCreateDto createValidCreateDto() {
        Map<String, QuizQuestionTranslationDto> translations = new HashMap<>();

        translations.put("bg", QuizQuestionTranslationDto.builder()
                .questionText("Нов въпрос на бг?")
                .optionA("А1")
                .optionB("А2")
                .optionC("А3")
                .optionD("А4")
                .build());

        translations.put("en", QuizQuestionTranslationDto.builder()
                .questionText("New question in en?")
                .optionA("A1")
                .optionB("A2")
                .optionC("A3")
                .optionD("A4")
                .build());

        return QuizQuestionCreateDto.builder()
                .craftId(craftId)
                .correctOptionIndex(2) // C
                .pointsReward(15)
                .translations(translations)
                .build();
    }

    private QuizQuestionCreateDto createValidUpdateDto() {
        Map<String, QuizQuestionTranslationDto> translations = new HashMap<>();

        translations.put("bg", QuizQuestionTranslationDto.builder()
                .questionText("Обновен въпрос на бг?")
                .optionA("Нова А1")
                .optionB("Нова А2")
                .optionC("Нова А3")
                .optionD("Нова А4")
                .build());

        translations.put("en", QuizQuestionTranslationDto.builder()
                .questionText("Updated question in en?")
                .optionA("New A1")
                .optionB("New A2")
                .optionC("New A3")
                .optionD("New A4")
                .build());

        return QuizQuestionCreateDto.builder()
                .craftId(craftId)
                .correctOptionIndex(2) // C
                .pointsReward(20)
                .translations(translations)
                .build();
    }
}