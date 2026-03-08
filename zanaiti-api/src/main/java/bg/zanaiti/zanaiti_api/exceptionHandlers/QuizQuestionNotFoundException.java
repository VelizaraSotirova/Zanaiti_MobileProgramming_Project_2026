package bg.zanaiti.zanaiti_api.exceptionHandlers;

public class QuizQuestionNotFoundException extends RuntimeException {
    public QuizQuestionNotFoundException(String message) {
        super(message);
    }
}
