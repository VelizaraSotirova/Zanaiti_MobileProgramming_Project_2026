package bg.zanaiti.zanaiti_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "languages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 5)
    private String code;  // 'bg', 'en'

    @Column(nullable = false)
    private String name;  // 'български', 'English'

    private boolean isActive = true;
}