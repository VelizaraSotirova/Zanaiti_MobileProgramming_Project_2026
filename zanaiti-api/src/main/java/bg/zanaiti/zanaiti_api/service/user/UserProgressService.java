package bg.zanaiti.zanaiti_api.service.user;

import bg.zanaiti.zanaiti_api.dto.LeaderboardEntryDto;
import bg.zanaiti.zanaiti_api.dto.UserProgressDto.UserProgressDto;
import bg.zanaiti.zanaiti_api.dto.UserProgressDto.UserProgressSummaryDto;
import bg.zanaiti.zanaiti_api.model.*;
import bg.zanaiti.zanaiti_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProgressService {

    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final CraftRepository craftRepository;
    private final LanguageRepository languageRepository;
    private final PointsHistoryRepository pointsHistoryRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    public List<UserProgressDto> getUserProgress(Long userId) {
        return progressRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserProgressDto getProgressForCraft(Long userId, Long craftId) {
        return progressRepository.findByUserIdAndCraftId(userId, craftId)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Transactional
    public UserProgressDto recordVisit(Long userId, Long craftId) {
        UserProgress progress = getOrCreateProgress(userId, craftId);
        progress.setAttemptCount(progress.getAttemptCount() + 1);
        progress.setLastInteractionDate(LocalDateTime.now());
        return convertToDto(progressRepository.save(progress));
    }

    @Transactional
    public UserProgressDto completeQuiz(Long userId, Long craftId, int correctAnswersCount, String languageCode) {
        UserProgress progress = getOrCreateProgress(userId, craftId);

        if (!progress.isQuizCompleted()) {
            Language language = languageRepository.findByCode(languageCode).orElse(null);

            // All active craft's questions
            List<QuizQuestion> questions = quizQuestionRepository.findByCraftIdAndIsActiveTrue(craftId);

            // Points calculation
            int pointsPerQuestion = questions.isEmpty() ? 10 : questions.getFirst().getPointsReward();
            int totalPoints = correctAnswersCount * pointsPerQuestion;

            progress.setQuizCompleted(true);
            progress.setQuizScore(correctAnswersCount);
            progress.setQuizCompletionDate(LocalDateTime.now());
            progress.setLanguage(language);
            progress.setLastInteractionDate(LocalDateTime.now());
            progress.setPointsEarned(progress.getPointsEarned() + totalPoints);

            // Update user total points
            User user = progress.getUser();
            user.setTotalPoints(user.getTotalPoints() + totalPoints);
            userRepository.save(user);

            // Save to points history
            String craftName = progress.getCraft().getTranslations().stream()
                    .findFirst()
                    .map(CraftTranslation::getName)
                    .orElse("Занаят #" + craftId);

            PointsHistory history = PointsHistory.builder()
                    .user(user)
                    .points(totalPoints)
                    .source("QUIZ")
                    .description("Успешен тест: " + craftName)
                    .craft(progress.getCraft())
                    .build();
            pointsHistoryRepository.save(history);
        }

        return convertToDto(progressRepository.save(progress));
    }

    public List<LeaderboardEntryDto> getLeaderboard() {
        List<Object[]> results = progressRepository.getLeaderboard();

        return results.stream()
                .map(row -> LeaderboardEntryDto.builder()
                        .userId(((Number) row[0]).longValue())
                        .username((String) row[1])
                        .totalPoints(((Number) row[2]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    public UserProgressSummaryDto getUserSummary(Long userId) {
        List<UserProgress> progresses = progressRepository.findByUserId(userId);

        int totalPoints = progresses.stream().mapToInt(UserProgress::getPointsEarned).sum();
        int craftsVisited = progresses.size();
        int quizzesCompleted = (int) progresses.stream().filter(UserProgress::isQuizCompleted).count();
        double averageScore = progresses.stream()
                .filter(p -> p.getQuizScore() != null)
                .mapToInt(UserProgress::getQuizScore)
                .average()
                .orElse(0.0);

        return UserProgressSummaryDto.builder()
                .totalPoints(totalPoints)
                .craftsVisited(craftsVisited)
                .quizzesCompleted(quizzesCompleted)
                .averageScore(averageScore)
                .build();
    }

    private UserProgress getOrCreateProgress(Long userId, Long craftId) {
        return progressRepository.findByUserIdAndCraftId(userId, craftId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    Craft craft = craftRepository.findById(craftId)
                            .orElseThrow(() -> new RuntimeException("Craft not found"));
                    return UserProgress.builder()
                            .user(user)
                            .craft(craft)
                            .pointsEarned(0)
                            .quizCompleted(false)
                            .attemptCount(0)
                            .lastInteractionDate(LocalDateTime.now())
                            .build();
                });
    }

    private UserProgressDto convertToDto(UserProgress progress) {
        String craftName = progress.getCraft().getTranslations().stream()
                .findFirst()
                .map(CraftTranslation::getName)
                .orElse("Unknown");

        return UserProgressDto.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .craftId(progress.getCraft().getId())
                .craftName(craftName)
                .pointsEarned(progress.getPointsEarned())
                .quizCompleted(progress.isQuizCompleted())
                .attemptCount(progress.getAttemptCount())
                .lastInteractionDate(progress.getLastInteractionDate())
                .quizCompletionDate(progress.getQuizCompletionDate())
                .quizScore(progress.getQuizScore())
                .languageCode(progress.getLanguage() != null ? progress.getLanguage().getCode() : null)
                .build();
    }
}