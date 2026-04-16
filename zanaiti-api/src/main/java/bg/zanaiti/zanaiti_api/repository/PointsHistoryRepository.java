package bg.zanaiti.zanaiti_api.repository;

import bg.zanaiti.zanaiti_api.model.PointsHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsHistoryRepository extends JpaRepository<PointsHistory, Long> {
    List<PointsHistory> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<PointsHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}