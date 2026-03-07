package bg.zanaiti.zanaiti_api.service.admin;

import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionCreateDto;
import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionDto;
import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionTranslationDto;
import bg.zanaiti.zanaiti_api.exceptionHandlers.DuplicateQuestionException;
import bg.zanaiti.zanaiti_api.model.*;
import bg.zanaiti.zanaiti_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminQuizService {

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizQuestionTranslationRepository translationRepository;
    private final CraftRepository craftRepository;
    private final LanguageRepository languageRepository;

    @Transactional
    public QuizQuestionDto createQuestion(QuizQuestionCreateDto dto) {
        String bulgarianQuestion = dto.getTranslations().get("bg").getQuestionText();
        if (quizQuestionRepository.existsDuplicateQuestion(dto.getCraftId(), bulgarianQuestion, "bg")) {
            throw new DuplicateQuestionException("Question '" + bulgarianQuestion + "' already exists for this craft!");
        }

        Craft craft = craftRepository.findById(dto.getCraftId())
                .orElseThrow(() -> new RuntimeException("Craft not found with id: " + dto.getCraftId()));

        QuizQuestion question = QuizQuestion.builder()
                .craft(craft)
                .correctOptionIndex(dto.getCorrectOptionIndex())
                .pointsReward(dto.getPointsReward() != null ? dto.getPointsReward() : 10)
                .isActive(true)
                .translations(new ArrayList<>())
                .build();

        QuizQuestion savedQuestion = quizQuestionRepository.save(question);
        saveTranslations(savedQuestion, dto.getTranslations());

        return mapToDto(savedQuestion);
    }

    @Transactional
    public QuizQuestionDto updateQuestion(Long id, QuizQuestionCreateDto dto) {
        QuizQuestion question = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));

        if (dto.getCorrectOptionIndex() != null) {
            question.setCorrectOptionIndex(dto.getCorrectOptionIndex());
        }
        if (dto.getPointsReward() != null) {
            question.setPointsReward(dto.getPointsReward());
        }
        if (dto.getCraftId() != null && !dto.getCraftId().equals(question.getCraft().getId())) {
            Craft craft = craftRepository.findById(dto.getCraftId())
                    .orElseThrow(() -> new RuntimeException("Craft not found"));
            question.setCraft(craft);
        }

        if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
            translationRepository.deleteAll(question.getTranslations());
            question.getTranslations().clear();
            saveTranslations(question, dto.getTranslations());
        }

        return mapToDto(quizQuestionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        QuizQuestion question = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));
        question.setActive(false);
        quizQuestionRepository.save(question);
    }

    private void saveTranslations(QuizQuestion question, Map<String, QuizQuestionTranslationDto> translations) {
        for (Map.Entry<String, QuizQuestionTranslationDto> entry : translations.entrySet()) {
            String languageCode = entry.getKey();
            QuizQuestionTranslationDto dto = entry.getValue();

            Language language = languageRepository.findByCode(languageCode)
                    .orElseThrow(() -> new RuntimeException("Language not found: " + languageCode));

            QuizQuestionTranslation translation = QuizQuestionTranslation.builder()
                    .quizQuestion(question)
                    .language(language)
                    .questionText(dto.getQuestionText())
                    .optionA(dto.getOptionA())
                    .optionB(dto.getOptionB())
                    .optionC(dto.getOptionC())
                    .optionD(dto.getOptionD())
                    .build();

            translationRepository.save(translation);
            question.getTranslations().add(translation);
        }
    }

    private QuizQuestionDto mapToDto(QuizQuestion question) {
        Map<String, QuizQuestionTranslationDto> translationsMap = new HashMap<>();
        for (QuizQuestionTranslation t : question.getTranslations()) {
            translationsMap.put(t.getLanguage().getCode(), mapTranslationToDto(t));
        }

        return QuizQuestionDto.builder()
                .id(question.getId())
                .craftId(question.getCraft().getId())
                .translations(translationsMap)
                .correctOptionIndex(question.getCorrectOptionIndex())
                .pointsReward(question.getPointsReward())
                .isActive(question.isActive())
                .build();
    }

    private QuizQuestionTranslationDto mapTranslationToDto(QuizQuestionTranslation translation) {
        return QuizQuestionTranslationDto.builder()
                .questionText(translation.getQuestionText())
                .optionA(translation.getOptionA())
                .optionB(translation.getOptionB())
                .optionC(translation.getOptionC())
                .optionD(translation.getOptionD())
                .build();
    }
}