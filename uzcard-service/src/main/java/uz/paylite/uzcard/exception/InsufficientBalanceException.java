package uz.paylite.uzcard.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String pan, Long amount) {
        super(
            "Insufficient balance for PAN: " +
            pan +
            ", requested amount: " +
            amount
        );
    }
}
