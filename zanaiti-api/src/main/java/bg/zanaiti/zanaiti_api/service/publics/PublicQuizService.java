package bg.zanaiti.zanaiti_api.service.publics;

import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionDto;
import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionTranslationDto;
import bg.zanaiti.zanaiti_api.model.QuizQuestion;
import bg.zanaiti.zanaiti_api.model.QuizQuestionTranslation;
import bg.zanaiti.zanaiti_api.repository.QuizQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicQuizService {

    private final QuizQuestionRepository quizQuestionRepository;

    public List<QuizQuestionDto> getQuestionsByCraftAndLanguage(Long craftId, String languageCode) {
        return quizQuestionRepository.findQuizQuestionsWithTranslations(craftId, languageCode)
                .stream()
                .map(q -> mapToDto(q, languageCode))
                .collect(Collectors.toList());
    }

    public boolean checkAnswer(Long questionId, int selectedOptionIndex) {
        QuizQuestion question = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("QuizQuestion not found"));
        return question.getCorrectOptionIndex().equals(selectedOptionIndex);
    }

    private QuizQuestionDto mapToDto(QuizQuestion question, String languageCode) {
        Map<String, QuizQuestionTranslationDto> translationsMap = new HashMap<>();

        question.getTranslations().stream()
                .filter(t -> t.getLanguage().getCode().equals(languageCode))
                .findFirst()
                .ifPresent(t -> translationsMap.put(languageCode, mapTranslationToDto(t)));

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