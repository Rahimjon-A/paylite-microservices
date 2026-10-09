package uz.paylite.cardbank.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

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
import uz.paylite.cardbank.util.CardNumberGenerator;
import uz.paylite.cardbank.web.rest.errors.CardBusinessException;

@Service
public class CardService {

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
        return switch (request.type()) {
            case UZCARD -> issueUzcard(request);
            case HUMO -> issueHumo(request);
        };
    }

    private CardResponse issueUzcard(CardCreateRequest request) {

        validateCustomerData(request);
        String pan = generateUniquePan(request.type());
        final CardAccountResponse account;

        try {
            account = uzcardClient.createAccount(new CreateCardAccountRequest(pan));
        } catch (Exception exception) {
            throw new CardIssuanceException("Unable to create UZCARD account", exception);
        }

        Card card = new Card()
            .pan(account.pan())
            .type(request.type())
            .expireDate(LocalDate.now().plusYears(CARD_VALIDITY_YEARS))
            .fullName(request.fullName()).pinfl(request.pinfl())
            .phoneNumber(request.phoneNumber())
            .status(CardStatus.ACTIVE)
            .createdAt(Instant.now());

        Card savedCard = cardRepository.save(card);

        payLiteClient.bindCard(
            new BindCardRequest(
                savedCard.getPan(),
                savedCard.getType().name(),
                savedCard.getExpireDate(),
                savedCard.getStatus().name(),
                savedCard.getCreatedAt()
            )
        );

        return toResponse(savedCard);
    }

    private CardResponse issueHumo(CardCreateRequest request) {
        validateCustomerData(request);
        String pan = generateUniquePan(request.type());
        final CardAccountResponse account;

        try {
            account = humoClient.createAccount(new CreateCardAccountRequest(pan));
        } catch (Exception exception) {
            throw new CardIssuanceException("Unable to create HUMO account", exception);
        }

        Card card = new Card()
            .pan(account.pan())
            .type(request.type())
            .expireDate(LocalDate.now().plusYears(CARD_VALIDITY_YEARS))
            .fullName(request.fullName())
            .pinfl(request.pinfl())
            .phoneNumber(request.phoneNumber())
            .status(CardStatus.ACTIVE)
            .createdAt(Instant.now());

        Card savedCard = cardRepository.save(card);

        payLiteClient.bindCard(
            new BindCardRequest(
                savedCard.getPan(),
                savedCard.getType().name(),
                savedCard.getExpireDate(),
                savedCard.getStatus().name(),
                savedCard.getCreatedAt()
            )
        );

        return toResponse(savedCard);
    }

    public CardAccountResponse deposit(String pan, Long amount) {
        validateAmount(amount);
        Card card = findActiveCard(pan);
        BalanceRequest request = new BalanceRequest(amount);

        return switch (card.getType()) {
            case UZCARD -> uzcardClient.deposit(pan, request);
            case HUMO -> humoClient.deposit(pan, request);
        };
    }

    public CardAccountResponse withdraw(String pan, Long amount) {
        validateAmount(amount);
        Card card = findActiveCard(pan);
        BalanceRequest request = new BalanceRequest(amount);

        return switch (card.getType()) {
            case UZCARD -> uzcardClient.withdraw(pan, request);
            case HUMO -> humoClient.withdraw(pan, request);
        };
    }

    public CardAccountResponse getBalance(String pan) {
        Card card = cardRepository.findByPan(pan)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Card not found"
            ));

        return switch (card.getType()) {
            case UZCARD -> uzcardClient.getAccount(pan);
            case HUMO -> humoClient.getAccount(pan);
        };
    }

    private Card findActiveCard(String pan) {
        Card card = cardRepository.findByPan(pan)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));

        if (card.getStatus() != CardStatus.ACTIVE) {
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
        }

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

    private CardResponse toResponse(Card card) {
        return new CardResponse(
            card.getId(),
            card.getPan(),
            card.getType(),
            card.getExpireDate(),
            card.getFullName(),
            card.getPhoneNumber(),
            card.getStatus(),
            card.getCreatedAt());
    }
}
