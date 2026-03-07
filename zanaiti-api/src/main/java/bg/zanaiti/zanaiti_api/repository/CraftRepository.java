package bg.zanaiti.zanaiti_api.repository;

import bg.zanaiti.zanaiti_api.model.Craft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CraftRepository extends JpaRepository<Craft, Long> {

    //Finding active crafts
    List<Craft> findByIsActiveTrue();

    // Finding carft in radius ( Google Maps )
    @Query("SELECT c FROM Craft c WHERE c.isActive = true AND " +
            "6371 * acos(cos(radians(:lat)) * cos(radians(c.latitude)) * " +
            "cos(radians(c.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(c.latitude))) <= :radius")
    List<Craft> findCraftsWithinRadius(@Param("lat") double latitude,
                                       @Param("lng") double longitude,
                                       @Param("radius") double radius);


    Optional<Craft> findByIdAndIsActiveTrue(Long id);
}

