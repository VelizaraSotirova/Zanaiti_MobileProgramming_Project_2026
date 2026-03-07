package bg.zanaiti.zanaiti_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "craft_id", nullable = false)
    private Craft craft; // which craft is questioned

    @Column(nullable = false)
    private Integer correctOptionIndex;  // Correct answer

    private Integer pointsReward = 10;  // Correct answer points

    private boolean isActive = true;

    @OneToMany(mappedBy = "quizQuestion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizQuestionTranslation> translations = new ArrayList<>();
}

