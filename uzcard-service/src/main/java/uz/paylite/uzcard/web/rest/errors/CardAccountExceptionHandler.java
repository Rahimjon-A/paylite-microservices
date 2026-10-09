package uz.paylite.uzcard.web.rest.errors;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.paylite.uzcard.exception.CardAccountAlreadyExistsException;
import uz.paylite.uzcard.exception.CardAccountNotFoundException;
import uz.paylite.uzcard.exception.InsufficientBalanceException;

@RestControllerAdvice
public class CardAccountExceptionHandler {

    @ExceptionHandler(CardAccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
        CardAccountNotFoundException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(CardAccountAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleAlreadyExists(
        CardAccountAlreadyExistsException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientBalance(
        InsufficientBalanceException exception
    ) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of("message", exception.getMessage()));
    }
}
