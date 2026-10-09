package uz.paylite.uzcard.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uz.paylite.uzcard.domain.CardAccountAsserts.*;
import static uz.paylite.uzcard.web.rest.TestUtil.createUpdateProxyForBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uz.paylite.uzcard.IntegrationTest;
import uz.paylite.uzcard.domain.CardAccount;
import uz.paylite.uzcard.domain.enumeration.CardAccountStatus;
import uz.paylite.uzcard.repository.CardAccountRepository;

/**
 * Integration tests for the {@link CardAccountResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CardAccountResourceIT {

    private static final String DEFAULT_PAN = "AAAAAAAAAAAAAAAA";
    private static final String UPDATED_PAN = "BBBBBBBBBBBBBBBB";

    private static final Long DEFAULT_BALANCE = 0L;
    private static final Long UPDATED_BALANCE = 1L;

    private static final CardAccountStatus DEFAULT_STATUS = CardAccountStatus.ACTIVE;
    private static final CardAccountStatus UPDATED_STATUS = CardAccountStatus.BLOCKED;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/card-accounts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CardAccountRepository cardAccountRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCardAccountMockMvc;

    private CardAccount cardAccount;

    private CardAccount insertedCardAccount;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CardAccount createEntity() {
        return new CardAccount()
            .pan(DEFAULT_PAN)
            .balance(DEFAULT_BALANCE)
            .status(DEFAULT_STATUS)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CardAccount createUpdatedEntity() {
        return new CardAccount()
            .pan(UPDATED_PAN)
            .balance(UPDATED_BALANCE)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    @BeforeEach
    void initTest() {
        cardAccount = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCardAccount != null) {
            cardAccountRepository.delete(insertedCardAccount);
            insertedCardAccount = null;
        }
    }

    @Test
    @Transactional
    void createCardAccount() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the CardAccount
        var returnedCardAccount = om.readValue(
            restCardAccountMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cardAccount))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CardAccount.class
        );

        // Validate the CardAccount in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertCardAccountUpdatableFieldsEquals(returnedCardAccount, getPersistedCardAccount(returnedCardAccount));

        insertedCardAccount = returnedCardAccount;
    }

    @Test
    @Transactional
    void createCardAccountWithExistingId() throws Exception {
        // Create the CardAccount with an existing ID
        cardAccount.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCardAccountMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cardAccount)))
            .andExpect(status().isBadRequest());

        // Validate the CardAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkPanIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cardAccount.setPan(null);

        // Create the CardAccount, which fails.

        restCardAccountMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cardAccount)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBalanceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cardAccount.setBalance(null);

        // Create the CardAccount, which fails.

        restCardAccountMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cardAccount)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cardAccount.setStatus(null);

        // Create the CardAccount, which fails.

        restCardAccountMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cardAccount)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cardAccount.setCreatedAt(null);

        // Create the CardAccount, which fails.

        restCardAccountMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cardAccount)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUpdatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cardAccount.setUpdatedAt(null);

        // Create the CardAccount, which fails.

        restCardAccountMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cardAccount)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCardAccounts() throws Exception {
        // Initialize the database
        insertedCardAccount = cardAccountRepository.saveAndFlush(cardAccount);

        // Get all the cardAccountList
        restCardAccountMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cardAccount.getId().intValue())))
            .andExpect(jsonPath("$.[*].pan").value(hasItem(DEFAULT_PAN)))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(DEFAULT_BALANCE.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @Test
    @Transactional
    void getCardAccount() throws Exception {
        // Initialize the database
        insertedCardAccount = cardAccountRepository.saveAndFlush(cardAccount);

        // Get the cardAccount
        restCardAccountMockMvc
            .perform(get(ENTITY_API_URL_ID, cardAccount.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cardAccount.getId().intValue()))
            .andExpect(jsonPath("$.pan").value(DEFAULT_PAN))
            .andExpect(jsonPath("$.balance").value(DEFAULT_BALANCE.intValue()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingCardAccount() throws Exception {
        // Get the cardAccount
        restCardAccountMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCardAccount() throws Exception {
        // Initialize the database
        insertedCardAccount = cardAccountRepository.saveAndFlush(cardAccount);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the cardAccount
        CardAccount updatedCardAccount = cardAccountRepository.findById(cardAccount.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCardAccount are not directly saved in db
        em.detach(updatedCardAccount);
        updatedCardAccount
            .pan(UPDATED_PAN)
            .balance(UPDATED_BALANCE)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restCardAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedCardAccount.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedCardAccount))
            )
            .andExpect(status().isOk());

        // Validate the CardAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCardAccountToMatchAllProperties(updatedCardAccount);
    }

    @Test
    @Transactional
    void putNonExistingCardAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cardAccount.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCardAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cardAccount.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(cardAccount))
            )
            .andExpect(status().isBadRequest());

        // Validate the CardAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCardAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cardAccount.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCardAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(cardAccount))
            )
            .andExpect(status().isBadRequest());

        // Validate the CardAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCardAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cardAccount.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCardAccountMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(cardAccount)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CardAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCardAccountWithPatch() throws Exception {
        // Initialize the database
        insertedCardAccount = cardAccountRepository.saveAndFlush(cardAccount);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the cardAccount using partial update
        CardAccount partialUpdatedCardAccount = new CardAccount();
        partialUpdatedCardAccount.setId(cardAccount.getId());

        partialUpdatedCardAccount.balance(UPDATED_BALANCE).status(UPDATED_STATUS);

        restCardAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCardAccount.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCardAccount))
            )
            .andExpect(status().isOk());

        // Validate the CardAccount in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCardAccountUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCardAccount, cardAccount),
            getPersistedCardAccount(cardAccount)
        );
    }

    @Test
    @Transactional
    void fullUpdateCardAccountWithPatch() throws Exception {
        // Initialize the database
        insertedCardAccount = cardAccountRepository.saveAndFlush(cardAccount);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the cardAccount using partial update
        CardAccount partialUpdatedCardAccount = new CardAccount();
        partialUpdatedCardAccount.setId(cardAccount.getId());

        partialUpdatedCardAccount
            .pan(UPDATED_PAN)
            .balance(UPDATED_BALANCE)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restCardAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCardAccount.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCardAccount))
            )
            .andExpect(status().isOk());

        // Validate the CardAccount in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCardAccountUpdatableFieldsEquals(partialUpdatedCardAccount, getPersistedCardAccount(partialUpdatedCardAccount));
    }

    @Test
    @Transactional
    void patchNonExistingCardAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cardAccount.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCardAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, cardAccount.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(cardAccount))
            )
            .andExpect(status().isBadRequest());

        // Validate the CardAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCardAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cardAccount.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCardAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(cardAccount))
            )
            .andExpect(status().isBadRequest());

        // Validate the CardAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCardAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cardAccount.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCardAccountMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(cardAccount))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CardAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCardAccount() throws Exception {
        // Initialize the database
        insertedCardAccount = cardAccountRepository.saveAndFlush(cardAccount);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the cardAccount
        restCardAccountMockMvc
            .perform(delete(ENTITY_API_URL_ID, cardAccount.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return cardAccountRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected CardAccount getPersistedCardAccount(CardAccount cardAccount) {
        return cardAccountRepository.findById(cardAccount.getId()).orElseThrow();
    }

    protected void assertPersistedCardAccountToMatchAllProperties(CardAccount expectedCardAccount) {
        assertCardAccountAllPropertiesEquals(expectedCardAccount, getPersistedCardAccount(expectedCardAccount));
    }

    protected void assertPersistedCardAccountToMatchUpdatableProperties(CardAccount expectedCardAccount) {
        assertCardAccountAllUpdatablePropertiesEquals(expectedCardAccount, getPersistedCardAccount(expectedCardAccount));
    }
}
