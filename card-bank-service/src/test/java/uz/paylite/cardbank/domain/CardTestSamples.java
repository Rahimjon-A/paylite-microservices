package uz.paylite.cardbank.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CardTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Card getCardSample1() {
        return new Card().id(1L).pan("pan1").fullName("fullName1").pinfl("pinfl1").phoneNumber("phoneNumber1");
    }

    public static Card getCardSample2() {
        return new Card().id(2L).pan("pan2").fullName("fullName2").pinfl("pinfl2").phoneNumber("phoneNumber2");
    }

    public static Card getCardRandomSampleGenerator() {
        return new Card()
            .id(longCount.incrementAndGet())
            .pan(UUID.randomUUID().toString())
            .fullName(UUID.randomUUID().toString())
            .pinfl(UUID.randomUUID().toString())
            .phoneNumber(UUID.randomUUID().toString());
    }
}
