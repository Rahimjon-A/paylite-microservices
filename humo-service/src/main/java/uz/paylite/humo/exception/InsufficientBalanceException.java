package uz.paylite.humo.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String pan, Long amount) {
        super("Insufficient balance for HUMO card: " + pan + ", requested amount: " + amount);
    }
}
