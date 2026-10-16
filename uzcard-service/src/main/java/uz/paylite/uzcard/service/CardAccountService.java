package uz.paylite.uzcard.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paylite.uzcard.domain.CardAccount;
import uz.paylite.uzcard.domain.dto.request.CreateCardAccountRequest;
import uz.paylite.uzcard.domain.dto.response.CardAccountResponse;
import uz.paylite.uzcard.domain.enumeration.CardAccountStatus;
import uz.paylite.uzcard.exception.CardAccountAlreadyExistsException;
import uz.paylite.uzcard.exception.CardAccountNotFoundException;
import uz.paylite.uzcard.exception.InsufficientBalanceException;
import uz.paylite.uzcard.repository.CardAccountRepository;

import static uz.paylite.uzcard.util.CardUtil.maskPan;


@Service
public class CardAccountService {

    private static final Logger log = LoggerFactory.getLogger(CardAccountService.class);
    private final CardAccountRepository cardAccountRepository;

    public CardAccountService(CardAccountRepository cardAccountRepository) {
        this.cardAccountRepository = cardAccountRepository;
    }

    @Transactional
    public CardAccountResponse create(CreateCardAccountRequest request) {
        log.debug("Creating Uzcard CardAccount for PAN ending in: {}", maskPan(request.pan()));

        if (cardAccountRepository.existsByPan(request.pan())) {
            log.warn("Failed to create Uzcard CardAccount: PAN ending in {} already exists", maskPan(request.pan()));
            throw new CardAccountAlreadyExistsException(request.pan());
        }

        Instant now = Instant.now();

        CardAccount account = new CardAccount()
            .pan(request.pan())
            .balance(0L)
            .status(CardAccountStatus.ACTIVE)
            .createdAt(now)
            .updatedAt(now);

        CardAccount saved = cardAccountRepository.save(account);
        log.info("Successfully created Uzcard CardAccount for PAN ending in: {}", maskPan(saved.getPan()));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CardAccountResponse getByPan(String pan) {
        log.debug("Retrieving Uzcard CardAccount for PAN ending in: {}", maskPan(pan));

        CardAccount account = cardAccountRepository.findByPan(pan).orElseThrow(() -> {
            log.warn("Uzcard CardAccount not found for PAN ending in: {}", maskPan(pan));
            return new CardAccountNotFoundException(pan);
        });

        return toResponse(account);
    }

    @Transactional
    public CardAccountResponse deposit(String pan, Long amount) {
        log.debug("Depositing {} to Uzcard CardAccount ending in: {}", amount, maskPan(pan));

        int updatedRows = cardAccountRepository.deposit(pan, amount);

        if (updatedRows == 0) {
            log.warn("Deposit failed: Uzcard CardAccount not found for PAN ending in: {}", maskPan(pan));
            throw new CardAccountNotFoundException(pan);
        }

        log.info("Successfully deposited {} to Uzcard CardAccount ending in: {}", amount, maskPan(pan));
        return getByPan(pan);
    }

    @Transactional
    public CardAccountResponse withdraw(String pan, Long amount) {
        log.debug("Withdrawing {} from Uzcard CardAccount ending in: {}", amount, maskPan(pan));

        int updatedRows = cardAccountRepository.withdraw(pan, amount);

        if (updatedRows == 0) {

            if (!cardAccountRepository.existsByPan(pan)) {
                log.warn("Withdraw failed: Uzcard CardAccount not found for PAN ending in: {}", maskPan(pan));
                throw new CardAccountNotFoundException(pan);
            }

            log.warn("Withdraw failed: Insufficient balance for Uzcard CardAccount ending in: {}. Requested: {}", maskPan(pan), amount);
            throw new InsufficientBalanceException(pan, amount);
        }

        log.info("Successfully withdrew {} from Uzcard CardAccount ending in: {}", amount, maskPan(pan));
        return getByPan(pan);
    }

    public void close(String pan) {
        log.debug("Closing Uzcard CardAccount ending in: {}", maskPan(pan));

        CardAccount account =
            cardAccountRepository
                .findByPan(pan)
                .orElseThrow(() -> {
                    log.warn("Close failed: Uzcard CardAccount not found for PAN ending in: {}", maskPan(pan));
                    return new CardAccountNotFoundException(pan);});

        account.setStatus(CardAccountStatus.CLOSED);

        account.setUpdatedAt(Instant.now());
        log.info("Successfully closed Uzcard CardAccount ending in: {}", maskPan(pan));
    }

    private CardAccountResponse toResponse(CardAccount account) {
        return new CardAccountResponse(
            account.getPan(),
            account.getBalance(),
            account.getStatus());
    }
}
