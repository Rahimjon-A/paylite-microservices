package uz.paylite.humo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uz.paylite.humo.domain.CardAccountTestSamples.*;

import org.junit.jupiter.api.Test;
import uz.paylite.humo.web.rest.TestUtil;

class CardAccountTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CardAccount.class);
        CardAccount cardAccount1 = getCardAccountSample1();
        CardAccount cardAccount2 = new CardAccount();
        assertThat(cardAccount1).isNotEqualTo(cardAccount2);

        cardAccount2.setId(cardAccount1.getId());
        assertThat(cardAccount1).isEqualTo(cardAccount2);

        cardAccount2 = getCardAccountSample2();
        assertThat(cardAccount1).isNotEqualTo(cardAccount2);
    }
}
