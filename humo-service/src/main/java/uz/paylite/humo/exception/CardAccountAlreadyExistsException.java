package uz.paylite.humo.exception;

public class CardAccountAlreadyExistsException extends RuntimeException {

    public CardAccountAlreadyExistsException(String pan) {
        super("HUMO card account already exists: " + pan);
    }
}
