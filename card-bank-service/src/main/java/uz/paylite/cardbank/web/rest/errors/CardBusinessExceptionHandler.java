package uz.paylite.cardbank.web.rest.errors;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CardBusinessExceptionHandler {

    @ExceptionHandler(CardBusinessException.class)
    public ResponseEntity<Map<String, String>> handleCardBusinessException(
        CardBusinessException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of("message", exception.getMessage()));
    }
}
