package bg.zanaiti.zanaiti_api.dto.User;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private int totalPoints;
    private LocalDateTime createdAt;
    private Set<String> roles;
    private boolean isActive;
}