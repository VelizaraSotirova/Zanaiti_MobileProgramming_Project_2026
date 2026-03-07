package bg.zanaiti.zanaiti_api.repository;

import bg.zanaiti.zanaiti_api.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    // All questions for a craft
    List<QuizQuestion> findByCraftId(Long craftId);
    // Only the active ones
    List<QuizQuestion> findByCraftIdAndIsActiveTrue(Long craftId);

    // Translated to the chosen language
    @Query("SELECT q FROM QuizQuestion q " +
            "LEFT JOIN FETCH q.translations t " +
            "LEFT JOIN FETCH t.language l " +
            "WHERE q.craft.id = :craftId " +
            "AND q.isActive = true " +
            "AND (l.code = :languageCode OR t IS NULL)")
    List<QuizQuestion> findQuizQuestionsWithTranslations(
            @Param("craftId") Long craftId,
            @Param("languageCode") String languageCode);


    @Query("SELECT COUNT(t) > 0 FROM QuizQuestionTranslation t " +
            "WHERE t.quizQuestion.craft.id = :craftId " +
            "AND t.questionText = :questionText " +
            "AND t.language.code = :languageCode " +
            "AND t.quizQuestion.isActive = true")
    boolean existsDuplicateQuestion(@Param("craftId") Long craftId,
                                    @Param("questionText") String questionText,
                                    @Param("languageCode") String languageCode);
}

