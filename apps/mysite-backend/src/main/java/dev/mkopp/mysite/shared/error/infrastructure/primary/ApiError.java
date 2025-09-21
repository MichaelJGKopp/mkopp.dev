package dev.mkopp.mysite.shared.error.infrastructure.primary;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Standard error response following RFC 7807 Problem Details for HTTP APIs.
 * Extended with Werkly-specific fields.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiError", description = "Standard error response")
public class ApiError extends ProblemDetail {
    @Value("${mysite.api.error.path}")
    private String apiErrorPath;

    private final String errorCode;
    private final Instant timestamp;
    private final String traceId;
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ValidationError> validationErrors;
    
    public ApiError(String errorCode, String title, HttpStatus status) {
        super(status.value());
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.traceId = org.slf4j.MDC.get("traceId");
        
        setTitle(title);
    }
    
    public void addValidationError(String field, String message) {
        if (validationErrors == null) {
            validationErrors = new ArrayList<>();
        }
        validationErrors.add(new ValidationError(field, message));
    }
    
    @Getter
    public static class ValidationError implements Serializable {
        private final String field;
        private final String message;
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}