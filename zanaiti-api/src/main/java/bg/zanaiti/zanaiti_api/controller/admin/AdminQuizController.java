package bg.zanaiti.zanaiti_api.controller.admin;

import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionCreateDto;
import bg.zanaiti.zanaiti_api.dto.QuizQuestionDto.QuizQuestionDto;
import bg.zanaiti.zanaiti_api.service.admin.AdminQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/quiz")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuizController {

    private final AdminQuizService adminQuizService;

    @PostMapping
    public ResponseEntity<QuizQuestionDto> createQuestion(@RequestBody QuizQuestionCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminQuizService.createQuestion(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizQuestionDto> updateQuestion(
            @PathVariable Long id,
            @RequestBody QuizQuestionCreateDto dto) {
        return ResponseEntity.ok(adminQuizService.updateQuestion(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        adminQuizService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}