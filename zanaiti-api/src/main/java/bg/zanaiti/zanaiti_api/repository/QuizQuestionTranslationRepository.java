package bg.zanaiti.zanaiti_api.repository;

import bg.zanaiti.zanaiti_api.model.QuizQuestionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizQuestionTranslationRepository extends JpaRepository<QuizQuestionTranslation, Long> {
    @Query("SELECT qt FROM QuizQuestionTranslation qt WHERE qt.quizQuestion.id = :questionId AND qt.language.code = :languageCode")
    Optional<QuizQuestionTranslation> findByQuizQuestionIdAndLanguageCode(@Param("questionId") Long questionId, @Param("languageCode") String languageCode);

    List<QuizQuestionTranslation> findByQuizQuestionId(Long questionId);
    List<QuizQuestionTranslation> findByLanguageCode(String languageCode);
}
