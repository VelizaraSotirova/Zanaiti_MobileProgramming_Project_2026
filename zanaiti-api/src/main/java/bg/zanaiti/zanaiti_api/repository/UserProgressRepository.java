package bg.zanaiti.zanaiti_api.repository;

import bg.zanaiti.zanaiti_api.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserId(Long userId);
    Optional<UserProgress> findByUserIdAndCraftId(Long userId, Long craftId);

    // Visited crafts
    List<UserProgress> findByUserIdAndQuizCompletedTrue(Long userId);

    // Total points for user ( if has points_earned )
    @Query("SELECT SUM(up.pointsEarned) FROM UserProgress up WHERE up.user.id = :userId")
    Integer getTotalPointsEarnedByUser(@Param("userId") Long userId);

    // Users leaderboard
    @Query("SELECT up.user.id, up.user.username, SUM(up.pointsEarned) as totalPoints " +
            "FROM UserProgress up " +
            "GROUP BY up.user.id, up.user.username " +
            "ORDER BY totalPoints DESC")
    List<Object[]> getLeaderboard();
}

