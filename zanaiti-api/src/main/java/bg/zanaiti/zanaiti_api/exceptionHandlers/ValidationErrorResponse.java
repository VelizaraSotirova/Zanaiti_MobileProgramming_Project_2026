package bg.zanaiti.zanaiti_api.exceptionHandlers;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ValidationErrorResponse {
    private String message;
    private Map<String, List<String>> errors;
    private LocalDateTime timestamp;
}
