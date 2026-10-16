package uz.paylite.cardbank.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uz.paylite.cardbank.client.HumoClient;
import uz.paylite.cardbank.client.PayLiteClient;
import uz.paylite.cardbank.client.UzcardClient;
import uz.paylite.cardbank.domain.Card;
import uz.paylite.cardbank.domain.dto.request.BalanceRequest;
import uz.paylite.cardbank.domain.dto.request.BindCardRequest;
import uz.paylite.cardbank.domain.dto.request.CardCreateRequest;
import uz.paylite.cardbank.domain.dto.request.CreateCardAccountRequest;
import uz.paylite.cardbank.domain.dto.response.CardAccountResponse;
import uz.paylite.cardbank.domain.dto.response.CardResponse;
import uz.paylite.cardbank.domain.enumeration.CardStatus;
import uz.paylite.cardbank.domain.enumeration.CardType;
import uz.paylite.cardbank.exception.CardIssuanceException;
import uz.paylite.cardbank.exception.CardNotFoundException;
import uz.paylite.cardbank.repository.CardRepository;
import uz.paylite.cardbank.security.SecurityUtils;
import uz.paylite.cardbank.util.CardNumberGenerator;
import uz.paylite.cardbank.web.rest.errors.CardBusinessException;

import static uz.paylite.cardbank.util.CardUtil.maskPan;

@Service
public class CardService {
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);
    private static final int CARD_VALIDITY_YEARS = 3;
    private static final int MAX_PAN_GENERATION_ATTEMPTS = 10;

    private final CardRepository cardRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final UzcardClient uzcardClient;
    private final HumoClient humoClient;
    private final PayLiteClient payLiteClient;

    public CardService( CardRepository cardRepository,
                        CardNumberGenerator cardNumberGenerator,
                        UzcardClient uzcardClient,
                        HumoClient humoClient,
                        PayLiteClient payLiteClient) {
        this.cardRepository = cardRepository;
        this.cardNumberGenerator = cardNumberGenerator;
        this.uzcardClient = uzcardClient;
        this.humoClient = humoClient;
        this.payLiteClient = payLiteClient;
    }

    @Transactional
    public CardResponse issueCard(CardCreateRequest request) {
        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("Current user is not authenticated"));

        LOG.info("Starting card issuance process for user: {}, Type: {}", login, request.type());

        validateCustomerData(request);

        String pan = generateUniquePan(request.type());
        LOG.debug("Generated unique PAN ending in: {} for new card", maskPan(pan));

        Card savedCard = cardRepository.saveAndFlush(getCard(request, login, pan));
        LOG.debug("Saved card to local repository with PAN ending in: {}", maskPan(pan));

        LOG.debug("Binding card to agent in paylite-service for PAN ending in: {}", maskPan(pan));
        payLiteClient.bindCard(
            new BindCardRequest(
                savedCard.getPan(),
                savedCard.getType().name(),
                savedCard.getExpireDate(),
                savedCard.getStatus().name(),
                savedCard.getCreatedAt(),
                savedCard.getCreatedByLogin()
            )
        );
        LOG.info("Successfully bound card to agent in paylite-service for PAN ending in: {}", maskPan(pan));

        createExternalAccount(request.type(), pan);

        LOG.info("Card issuance process completed successfully for PAN ending in: {}", maskPan(pan));
        return toResponse(savedCard);
    }

    private CardAccountResponse createExternalAccount(CardType type, String pan) {
        CreateCardAccountRequest accountRequest = new CreateCardAccountRequest(pan);
        LOG.debug("Creating external account in {}-service for PAN ending in: {}", type, maskPan(pan));
        try {
            CardAccountResponse response = switch (type) {
                case UZCARD -> uzcardClient.createAccount(accountRequest);
                case HUMO -> humoClient.createAccount(accountRequest);
            };
            LOG.info("Successfully created external account in {}-service for PAN ending in: {}", type, maskPan(pan));
            return response;
        } catch (Exception exception) {
            LOG.error("Failed to create external account in {}-service for PAN ending in: {}. Starting compensation...", type, maskPan(pan), exception);
            try {
                payLiteClient.unbindCard(pan);
                LOG.info("Successfully compensated (unbound) card ending in: {} in paylite-service", maskPan(pan));
            } catch (Exception compensationEx) {
                LOG.error("Failed to compensate (unbind) card ending in: {} in paylite-service", maskPan(pan), compensationEx);
            }
            throw new CardIssuanceException("Unable to create " + type + " account", exception);
        }
    }

    public CardAccountResponse deposit(String pan, Long amount) {
        LOG.debug("Initiating deposit of {} for PAN ending in: {}", amount, maskPan(pan));
        validateAmount(amount);
        Card card = findActiveCard(pan);
        BalanceRequest request = new BalanceRequest(amount);

        LOG.debug("Routing deposit to {}-service for PAN ending in: {}", card.getType(), maskPan(pan));
        CardAccountResponse response = switch (card.getType()) {
            case UZCARD -> uzcardClient.deposit(pan, request);
            case HUMO -> humoClient.deposit(pan, request);
        };
        LOG.info("Successfully deposited {} to PAN ending in: {} via {}", amount, maskPan(pan), card.getType());
        return response;
    }

    public CardAccountResponse withdraw(String pan, Long amount) {
        LOG.debug("Initiating withdrawal of {} for PAN ending in: {}", amount, maskPan(pan));
        validateAmount(amount);
        Card card = findActiveCard(pan);
        BalanceRequest request = new BalanceRequest(amount);

        LOG.debug("Routing withdrawal to {}-service for PAN ending in: {}", card.getType(), maskPan(pan));
        CardAccountResponse response = switch (card.getType()) {
            case UZCARD -> uzcardClient.withdraw(pan, request);
            case HUMO -> humoClient.withdraw(pan, request);
        };
        LOG.info("Successfully withdrew {} from PAN ending in: {} via {}", amount, maskPan(pan), card.getType());
        return response;
    }

    public CardAccountResponse getBalance(String pan) {
        LOG.debug("Retrieving balance for PAN ending in: {}", maskPan(pan));
        Card card = cardRepository.findByPan(pan)
            .orElseThrow(() -> {
                LOG.warn("Balance check failed: Card not found for PAN ending in: {}", maskPan(pan));
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found");
            });

        LOG.debug("Routing balance check to {}-service for PAN ending in: {}", card.getType(), maskPan(pan));
        return switch (card.getType()) {
            case UZCARD -> uzcardClient.getAccount(pan);
            case HUMO -> humoClient.getAccount(pan);
        };
    }

    private Card findActiveCard(String pan) {
        Card card = cardRepository.findByPan(pan)
            .orElseThrow(() -> {
                LOG.warn("Active card check failed: Card not found for PAN ending in: {}", maskPan(pan));
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found");
            });

        if (card.getStatus() != CardStatus.ACTIVE) {
            LOG.warn("Active card check failed: Card ending in {} is in {} status", maskPan(pan), card.getStatus());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card is not active");
        }

        return card;
    }

    private void validateAmount(Long amount) {
        if (amount == null || amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
    }

    @Transactional(readOnly = true)
    public CardResponse findByPan(String pan) {
        Card card = cardRepository.findByPan(pan)
            .orElseThrow(() -> new CardNotFoundException(pan));

        return toResponse(card);
    }

    @Transactional(readOnly = true)
    public Optional<Card> findById(Long id) {
        return cardRepository.findById(id);
    }

    private String generateUniquePan(CardType type) {
        for (int attempt = 0; attempt < MAX_PAN_GENERATION_ATTEMPTS; attempt++) {
            String pan = cardNumberGenerator.generate(type);

            if (!cardRepository.existsByPan(pan)) {
                return pan;
            }
            LOG.warn("PAN collision detected on attempt {}. Regenerating...", attempt + 1);
        }

        LOG.error("Failed to generate unique PAN after {} attempts", MAX_PAN_GENERATION_ATTEMPTS);
        throw new CardBusinessException("Could not generate a unique card number");
    }

    /**
     * TODO ask is it possible to have multiple cards by one phone number and pinfl
     */
    private void validateCustomerData(CardCreateRequest request) {
        if (cardRepository.existsByPinfl(request.pinfl())) {
            throw new CardBusinessException("A card already exists for this PINFL");
        }

        if (cardRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new CardBusinessException("A card already exists for this phone number");
        }
    }

    private Card getCard(CardCreateRequest request, String login, String pan) {
        return new Card()
            .pan(pan)
            .type(request.type())
            .expireDate(LocalDate.now().plusYears(CARD_VALIDITY_YEARS))
            .fullName(request.fullName()).pinfl(request.pinfl())
            .phoneNumber(request.phoneNumber())
            .status(CardStatus.ACTIVE)
            .createdAt(Instant.now())
            .createdByLogin(login);
    }

    private CardResponse toResponse(Card card) {
        return new CardResponse(
            card.getId(),
            card.getPan(),
            card.getType(),
            card.getExpireDate(),
            card.getFullName(),
            card.getPhoneNumber(),
            card.getStatus(),
            card.getCreatedAt(),
            card.getCreatedByLogin());
    }
}
