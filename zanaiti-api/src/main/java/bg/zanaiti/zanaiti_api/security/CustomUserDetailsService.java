package bg.zanaiti.zanaiti_api.security;

import bg.zanaiti.zanaiti_api.model.Role;
import bg.zanaiti.zanaiti_api.model.User;
import bg.zanaiti.zanaiti_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
//
//        return new CustomUserDetails(user);  // ← return CustomUserDetails, not User
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("========== LOADING USER ==========");
        System.out.println("Looking for username: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("USER NOT FOUND: " + username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        System.out.println("User found: " + user.getUsername());
        System.out.println("Password hash: " + user.getPassword());
        System.out.println("Roles: " + user.getRoles().stream().map(Role::getName).toList());
        System.out.println("==================================");

        return new CustomUserDetails(user);
    }
}