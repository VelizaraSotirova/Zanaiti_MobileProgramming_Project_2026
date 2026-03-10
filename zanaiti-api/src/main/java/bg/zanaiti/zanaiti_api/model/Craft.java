package bg.zanaiti.zanaiti_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crafts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Craft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;
    private String animationUrl;

    private Double latitude;
    private Double longitude;

    // The admin can deactivate a craft
    private boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // Translation link - one craft has many translations
    @OneToMany(mappedBy = "craft", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CraftTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "craft", cascade = CascadeType.ALL)
    private List<QuizQuestion> quizQuestions;

    @OneToMany(mappedBy = "craft", cascade = CascadeType.ALL)
    private List<UserProgress> progressList;
}

