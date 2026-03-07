package bg.zanaiti.zanaiti_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "craft_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Points from this concrete craft
    private int pointsEarned;

    // if the quiz is completed by the user
    private boolean quizCompleted = false;

    // Attempts per quiz
    private int attemptCount;

    // last visit of the craft
    private LocalDateTime lastInteractionDate;

    // when the quiz is completed (if it is)
    private LocalDateTime quizCompletionDate;

    private Integer quizScore;  // nullable, because quiz may not be completed


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "craft_id", nullable = false)
    private Craft craft;

    // Which language is the quiz (if completed)
    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;
}