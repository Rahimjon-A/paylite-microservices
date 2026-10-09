package uz.paylite.cardbank.web.rest.errors;

public class CardBusinessException extends RuntimeException {

    public CardBusinessException(String message) {
        super(message);
    }
}
