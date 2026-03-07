package bg.zanaiti.zanaiti_api.dto.User;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    private String fullName;
    private String email;
    private String password;
}