package uz.paylite.cardbank.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;
import uz.paylite.cardbank.domain.Card;
import uz.paylite.cardbank.domain.dto.request.BalanceRequest;
import uz.paylite.cardbank.domain.dto.request.CardCreateRequest;
import uz.paylite.cardbank.domain.dto.response.CardAccountResponse;
import uz.paylite.cardbank.domain.dto.response.CardResponse;
import uz.paylite.cardbank.repository.CardRepository;
import uz.paylite.cardbank.service.CardService;
import uz.paylite.cardbank.web.rest.errors.BadRequestAlertException;

/**
 * REST controller for managing {@link uz.paylite.cardbank.domain.Card}.
 */
@RestController
@RequestMapping("/api/cards")
public class CardResource {
    private static final Logger LOG = LoggerFactory.getLogger(CardResource.class);
    private final CardService cardService;

    public CardResource( CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardCreateRequest request) throws URISyntaxException {
        LOG.debug("REST request to create Card : {}", request);

        CardResponse response = cardService.issueCard(request);

        return ResponseEntity
            .created(new URI("/api/cards/" + response.pan()))
            .body(response);
    }

    @GetMapping("/{pan}")
    public ResponseEntity<CardResponse> getByPan(@PathVariable("pan") String pan) {
        LOG.debug("REST request to get Card with pan : {}", pan);

        CardResponse response = cardService.findByPan(pan);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{pan}/deposit")
    public ResponseEntity<CardAccountResponse> deposit(@PathVariable String pan,
                                                       @Valid @RequestBody BalanceRequest request) {
        return ResponseEntity.ok(cardService.deposit(pan, request.amount()));
    }

    @PostMapping("/{pan}/withdraw")
    public ResponseEntity<CardAccountResponse> withdraw(@PathVariable String pan,
                                                        @Valid @RequestBody BalanceRequest request) {
        return ResponseEntity.ok(cardService.withdraw(pan, request.amount()));
    }

    @GetMapping("/{pan}/balance")
    public ResponseEntity<CardAccountResponse> getBalance(@PathVariable String pan) {
        return ResponseEntity.ok(cardService.getBalance(pan));
    }


}
