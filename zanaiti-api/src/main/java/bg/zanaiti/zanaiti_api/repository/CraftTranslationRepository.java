package bg.zanaiti.zanaiti_api.repository;

import bg.zanaiti.zanaiti_api.model.CraftTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CraftTranslationRepository extends JpaRepository<CraftTranslation, Long> {
    Optional<CraftTranslation> findByCraftIdAndLanguageCode(Long craftId, String languageCode);
    boolean existsByNameAndLanguageCode(String name, String languageCode);
}