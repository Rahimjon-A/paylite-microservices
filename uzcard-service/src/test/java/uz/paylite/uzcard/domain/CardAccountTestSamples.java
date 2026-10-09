package uz.paylite.uzcard.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CardAccountTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static CardAccount getCardAccountSample1() {
        return new CardAccount().id(1L).pan("pan1").balance(1L);
    }

    public static CardAccount getCardAccountSample2() {
        return new CardAccount().id(2L).pan("pan2").balance(2L);
    }

    public static CardAccount getCardAccountRandomSampleGenerator() {
        return new CardAccount().id(longCount.incrementAndGet()).pan(UUID.randomUUID().toString()).balance(longCount.incrementAndGet());
    }
}
