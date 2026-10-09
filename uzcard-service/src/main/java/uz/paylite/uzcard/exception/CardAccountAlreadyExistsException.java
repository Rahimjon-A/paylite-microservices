package uz.paylite.uzcard.exception;

public class CardAccountAlreadyExistsException extends RuntimeException {

    public CardAccountAlreadyExistsException(String pan) {
        super("UZCARD account already exists for PAN: " + pan);
    }
}
