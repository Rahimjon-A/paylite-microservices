package uz.paylite.cardbank.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uz.paylite.cardbank.domain.CardTestSamples.*;

import org.junit.jupiter.api.Test;
import uz.paylite.cardbank.web.rest.TestUtil;

class CardTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Card.class);
        Card card1 = getCardSample1();
        Card card2 = new Card();
        assertThat(card1).isNotEqualTo(card2);

        card2.setId(card1.getId());
        assertThat(card1).isEqualTo(card2);

        card2 = getCardSample2();
        assertThat(card1).isNotEqualTo(card2);
    }
}
