package uz.paylite.uzcard.exception;

public class CardAccountNotFoundException extends RuntimeException {

    public CardAccountNotFoundException(String pan) {
        super("UZCARD account not found for PAN: " + pan);
    }
}
