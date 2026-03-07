package bg.zanaiti.zanaiti_api.exceptionHandlers;

public class DuplicateQuestionException extends RuntimeException {
    public DuplicateQuestionException(String message) {
        super(message);
    }
}
