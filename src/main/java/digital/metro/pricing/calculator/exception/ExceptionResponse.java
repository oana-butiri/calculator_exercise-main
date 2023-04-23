package digital.metro.pricing.calculator.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExceptionResponse {
    public static final String DEFAULT_ERROR_CODE = "100";
    private String message;
    private String code;


    public ExceptionResponse(String message) {
        this.message = message;
        this.code = DEFAULT_ERROR_CODE;
    }
}
