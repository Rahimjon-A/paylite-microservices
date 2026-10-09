package uz.paylite.humo.exception;

public class CardAccountNotFoundException extends RuntimeException {

    public CardAccountNotFoundException(String pan) {
        super("HUMO card account not found: " + pan);
    }
}
