package uz.paylite.cardbank.exception;

public class CardIssuanceException extends RuntimeException {

    public CardIssuanceException(String message) {
        super(message);
    }

    public CardIssuanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
