package bg.zanaiti.zanaiti_api.repository;

import bg.zanaiti.zanaiti_api.model.CraftTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CraftTranslationRepository extends JpaRepository<CraftTranslation, Long> {
    @Query("SELECT ct FROM CraftTranslation ct WHERE ct.craft.id = :craftId AND ct.language.code = :languageCode")
    Optional<CraftTranslation> findByCraftIdAndLanguageCode(@Param("craftId") Long craftId, @Param("languageCode") String languageCode);
    boolean existsByNameAndLanguageCode(String name, String languageCode);
}