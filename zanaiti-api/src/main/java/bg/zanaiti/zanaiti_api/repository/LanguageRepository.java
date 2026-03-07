package bg.zanaiti.zanaiti_api.repository;

import bg.zanaiti.zanaiti_api.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    Optional<Language> findByCode(String code);
    boolean existsByCode(String code);
    Optional<Language> findByCodeAndIsActiveTrue(String code);
}