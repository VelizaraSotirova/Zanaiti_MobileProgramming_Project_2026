package bg.zanaiti.zanaiti_api.exceptionHandlers;

public class DuplicateCraftException extends RuntimeException {
    public DuplicateCraftException(String message) {
        super(message);
    }
}
