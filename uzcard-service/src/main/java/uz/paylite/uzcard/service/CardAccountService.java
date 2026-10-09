package uz.paylite.uzcard.service;

import java.time.Instant;

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


@Service
public class CardAccountService {

    private final CardAccountRepository cardAccountRepository;

    public CardAccountService(CardAccountRepository cardAccountRepository) {
        this.cardAccountRepository = cardAccountRepository;
    }

    @Transactional
    public CardAccountResponse create(CreateCardAccountRequest request) {

        if (cardAccountRepository.existsByPan(request.pan())) {
            throw new CardAccountAlreadyExistsException(request.pan());
        }

        Instant now = Instant.now();

        CardAccount account = new CardAccount().pan(request.pan()).balance(0L).status(CardAccountStatus.ACTIVE).createdAt(now).updatedAt(now);

        CardAccount saved = cardAccountRepository.save(account);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CardAccountResponse getByPan(String pan) {

        CardAccount account = cardAccountRepository.findByPan(pan).orElseThrow(() -> new CardAccountNotFoundException(pan));

        return toResponse(account);
    }

    @Transactional
    public CardAccountResponse deposit(String pan, Long amount) {

        int updatedRows = cardAccountRepository.deposit(pan, amount);

        if (updatedRows == 0) {
            throw new CardAccountNotFoundException(pan);
        }

        return getByPan(pan);
    }

    @Transactional
    public CardAccountResponse withdraw(String pan, Long amount) {

        int updatedRows = cardAccountRepository.withdraw(pan, amount);

        if (updatedRows == 0) {

            if (!cardAccountRepository.existsByPan(pan)) {
                throw new CardAccountNotFoundException(pan);
            }

            throw new InsufficientBalanceException(pan, amount);
        }

        return getByPan(pan);
    }

    public void close(String pan) {
        CardAccount account =
            cardAccountRepository
                .findByPan(pan)
                .orElseThrow(() ->
                    new CardAccountNotFoundException(pan)
                );

        account.setStatus(CardAccountStatus.CLOSED);

        account.setUpdatedAt(Instant.now());
    }

    private CardAccountResponse toResponse(CardAccount account) {
        return new CardAccountResponse(
            account.getPan(),
            account.getBalance(),
            account.getStatus());
    }
}
