package bg.zanaiti.zanaiti_api.repository;

import org.springframework.stereotype.Repository;

import bg.zanaiti.zanaiti_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
