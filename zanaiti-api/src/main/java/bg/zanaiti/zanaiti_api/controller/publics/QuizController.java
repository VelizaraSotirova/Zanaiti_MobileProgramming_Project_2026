package bg.zanaiti.zanaiti_api.controller;

import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionDto;
import bg.zanaiti.zanaiti_api.service.publics.PublicQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final PublicQuizService quizService;

    @GetMapping("/craft/{craftId}")
    public ResponseEntity<List<QuizQuestionDto>> getQuestionsForCraft(
            @PathVariable Long craftId,
            @RequestParam(defaultValue = "bg") String lang) {
        return ResponseEntity.ok(quizService.getQuestionsByCraftAndLanguage(craftId, lang));
    }

    @PostMapping("/{questionId}/check")
    public ResponseEntity<Map<String, Object>> checkAnswer(
            @PathVariable Long questionId,
            @RequestParam int selectedOption) {
        boolean isCorrect = quizService.checkAnswer(questionId, selectedOption);
        return ResponseEntity.ok(Map.of(
                "correct", isCorrect,
                "message", isCorrect ? "Bravo!" : "Try again!"
        ));
    }
}