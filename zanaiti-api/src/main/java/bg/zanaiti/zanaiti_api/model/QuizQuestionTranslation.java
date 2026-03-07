package bg.zanaiti.zanaiti_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_question_translations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_question_id", nullable = false)
    private QuizQuestion quizQuestion;

    @ManyToOne
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(nullable = false, length = 2000)
    private String questionText;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}