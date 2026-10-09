package uz.paylite.cardbank.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;
import uz.paylite.cardbank.domain.enumeration.CardType;

@Component
public class CardNumberGenerator {

    private static final int PAN_LENGTH = 16;
    private static final int PREFIX_LENGTH = 4;

    private static final String UZCARD_PREFIX = "8600";
    private static final String HUMO_PREFIX = "9860";

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate(CardType cardType) {
        String prefix = getPrefix(cardType);

        String body = prefix + generateDigits(PAN_LENGTH - PREFIX_LENGTH - 1);

        int checkDigit = calculateLuhnCheckDigit(body);

        return body + checkDigit;
    }

    private String getPrefix(CardType cardType) {
        return switch (cardType) {
            case UZCARD -> UZCARD_PREFIX;
            case HUMO -> HUMO_PREFIX;
        };
    }

    private String generateDigits(int length) {
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            result.append(secureRandom.nextInt(10));
        }

        return result.toString();
    }

    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean doubleDigit = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.digit(number.charAt(i), 10);

            if (doubleDigit) {
                digit *= 2;

                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return (10 - (sum % 10)) % 10;
    }
}
