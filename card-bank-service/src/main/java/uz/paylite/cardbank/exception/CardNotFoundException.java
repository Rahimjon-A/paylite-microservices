package uz.paylite.cardbank.exception;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(String pan) {
        super("Card not found: " + pan);
    }
}
