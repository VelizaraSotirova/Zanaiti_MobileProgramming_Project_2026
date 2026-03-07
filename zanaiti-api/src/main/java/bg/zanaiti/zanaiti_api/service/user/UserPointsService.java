package bg.zanaiti.zanaiti_api.service.user;

import bg.zanaiti.zanaiti_api.dto.PointsHistory.PointsHistoryDto;
import bg.zanaiti.zanaiti_api.model.CraftTranslation;
import bg.zanaiti.zanaiti_api.model.PointsHistory;
import bg.zanaiti.zanaiti_api.repository.PointsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPointsService {

    private final PointsHistoryRepository pointsHistoryRepository;

    public List<PointsHistoryDto> getUserHistory(Long userId) {
        return pointsHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PointsHistoryDto> getUserHistoryLastDays(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return pointsHistoryRepository.findByUserIdAndCreatedAtBetween(userId, startDate, LocalDateTime.now())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Integer getTotalPointsLastDays(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return pointsHistoryRepository.findByUserIdAndCreatedAtBetween(userId, startDate, LocalDateTime.now())
                .stream()
                .mapToInt(PointsHistory::getPoints)
                .sum();
    }

    private PointsHistoryDto convertToDto(PointsHistory history) {
        String craftName = null;
        if (history.getCraft() != null && !history.getCraft().getTranslations().isEmpty()) {
            craftName = history.getCraft().getTranslations().stream()
                    .findFirst()
                    .map(CraftTranslation::getName)
                    .orElse(null);
        }

        return PointsHistoryDto.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .username(history.getUser().getUsername())
                .points(history.getPoints())
                .source(history.getSource())
                .description(history.getDescription())
                .createdAt(history.getCreatedAt())
                .craftId(history.getCraft() != null ? history.getCraft().getId() : null)
                .craftName(craftName)
                .build();
    }
}