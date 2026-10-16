package uz.paylite.humo.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.paylite.humo.domain.CardAccount;
import uz.paylite.humo.domain.dto.request.CreateCardAccountRequest;
import uz.paylite.humo.domain.dto.response.CardAccountResponse;
import uz.paylite.humo.domain.enumeration.CardAccountStatus;
import uz.paylite.humo.exception.CardAccountAlreadyExistsException;
import uz.paylite.humo.exception.CardAccountNotFoundException;
import uz.paylite.humo.exception.InsufficientBalanceException;
import uz.paylite.humo.repository.CardAccountRepository;

import static uz.paylite.humo.util.CardUtil.maskPan;


@Service
public class CardAccountService {

    private static final Logger log = LoggerFactory.getLogger(CardAccountService.class);
    private final CardAccountRepository cardAccountRepository;

    public CardAccountService(CardAccountRepository cardAccountRepository) {
        this.cardAccountRepository = cardAccountRepository;
    }

    @Transactional
    public CardAccountResponse create(CreateCardAccountRequest request) {
        log.debug("Creating Humo CardAccount for PAN ending in: {}", maskPan(request.pan()));

        if (cardAccountRepository.existsByPan(request.pan())) {
            log.warn("Failed to create Humo CardAccount: PAN ending in {} already exists", maskPan(request.pan()));
            throw new CardAccountAlreadyExistsException(request.pan());
        }

        Instant now = Instant.now();

        CardAccount account = new CardAccount()
            .pan(request.pan())
            .balance(0L)
            .status(CardAccountStatus.ACTIVE)
            .createdAt(now).updatedAt(now);

        CardAccount saved = cardAccountRepository.save(account);
        log.info("Successfully created Humo CardAccount for PAN ending in: {}", maskPan(saved.getPan()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CardAccountResponse getByPan(String pan) {
        log.debug("Retrieving Humo CardAccount for PAN ending in: {}", maskPan(pan));

        CardAccount account = cardAccountRepository
            .findByPan(pan)
            .orElseThrow(() -> {
                log.error("Humo CardAccount not found for PAN ending in: {}", maskPan(pan));
                return new CardAccountNotFoundException(pan);
            });

        return toResponse(account);
    }

    @Transactional
    public CardAccountResponse deposit(String pan, Long amount) {
        log.debug("Depositing {} to Humo CardAccount ending in: {}", amount, maskPan(pan));

        int updatedRows = cardAccountRepository.deposit(pan, amount);

        if (updatedRows == 0) {
            log.warn("Deposit failed: Humo CardAccount not found for PAN ending in: {}", maskPan(pan));
            throw new CardAccountNotFoundException(pan);
        }

        log.info("Successfully deposited {} to Humo CardAccount ending in: {}", amount, maskPan(pan));
        return getByPan(pan);
    }

    @Transactional
    public CardAccountResponse withdraw(String pan, Long amount) {
        log.debug("Withdrawing {} from Humo CardAccount ending in: {}", amount, maskPan(pan));

        int updatedRows = cardAccountRepository.withdraw(pan, amount);

        if (updatedRows == 0) {
            if (!cardAccountRepository.existsByPan(pan)) {
                log.warn("Withdraw failed: Humo CardAccount not found for PAN ending in: {}", maskPan(pan));
                throw new CardAccountNotFoundException(pan);
            }

            log.warn("Withdraw failed: Insufficient balance for Humo CardAccount ending in: {}. Requested: {}", maskPan(pan), amount);
            throw new InsufficientBalanceException(pan, amount);
        }

        log.info("Successfully withdrew {} from Humo CardAccount ending in: {}", amount, maskPan(pan));
        return getByPan(pan);
    }

    public void close(String pan) {
        log.debug("Closing Humo CardAccount ending in: {}", maskPan(pan));

        CardAccount account = cardAccountRepository
            .findByPan(pan)
            .orElseThrow(() -> {
                log.warn("Close failed: Humo CardAccount not found for PAN ending in: {}", maskPan(pan));
                return new CardAccountNotFoundException(pan);
            });

        account.setStatus(CardAccountStatus.CLOSED);
        account.setUpdatedAt(Instant.now());

        cardAccountRepository.save(account);
        log.info("Successfully closed Humo CardAccount ending in: {}", maskPan(pan));
    }

    private CardAccountResponse toResponse(CardAccount account) {
        return new CardAccountResponse(
            account.getPan(),
            account.getBalance(),
            account.getStatus());
    }
}
