package bg.zanaiti.zanaiti_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "craft_translations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CraftTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "craft_id", nullable = false)
    private Craft craft;

    @ManyToOne
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(length = 5000)
    private String historicalFacts;

    @Column(length = 5000)
    private String makingProcess; // Description of the craft's process
}