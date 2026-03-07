package bg.zanaiti.zanaiti_api.repository;

import bg.zanaiti.zanaiti_api.model.QuizQuestionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizQuestionTranslationRepository extends JpaRepository<QuizQuestionTranslation, Long> {
    Optional<QuizQuestionTranslation> findByQuizQuestionIdAndLanguageCode(Long questionId, String languageCode);
    List<QuizQuestionTranslation> findByQuizQuestionId(Long questionId);
    List<QuizQuestionTranslation> findByLanguageCode(String languageCode);
}
