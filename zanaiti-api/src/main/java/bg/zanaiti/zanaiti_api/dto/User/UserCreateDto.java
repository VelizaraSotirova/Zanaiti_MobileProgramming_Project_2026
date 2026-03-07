package bg.zanaiti.zanaiti_api.dto.User;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDto {
    private String username;
    private String email;
    private String password;
    private String fullName;
}