package uz.paylite.uzcard.web.rest;

import jakarta.validation.Valid;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.paylite.uzcard.domain.dto.request.BalanceRequest;
import uz.paylite.uzcard.domain.dto.request.CreateCardAccountRequest;
import uz.paylite.uzcard.domain.dto.response.CardAccountResponse;
import uz.paylite.uzcard.service.CardAccountService;

import static uz.paylite.uzcard.util.CardUtil.maskPan;

@RestController
@RequestMapping("/api/uzcard/card-accounts")
public class CardAccountResource {

    private static final Logger log = LoggerFactory.getLogger(CardAccountResource.class);
    private final CardAccountService cardAccountService;

    public CardAccountResource(CardAccountService cardAccountService) {
        this.cardAccountService = cardAccountService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<CardAccountResponse> create(@Valid @RequestBody CreateCardAccountRequest request) throws URISyntaxException {
        log.debug("REST request to create Uzcard CardAccount for PAN ending in : {}", maskPan(request.pan()));
        CardAccountResponse response = cardAccountService.create(request);

        return ResponseEntity.created(new URI("/api/uzcard/card-accounts/" + response.pan())).body(response);
    }

    @GetMapping("/{pan}")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<CardAccountResponse> getByPan(@PathVariable String pan) {
        log.debug("REST request to get Uzcard CardAccount : {}", maskPan(pan));
        return ResponseEntity.ok(cardAccountService.getByPan(pan));
    }

    @PostMapping("/{pan}/deposit")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<CardAccountResponse> deposit(@PathVariable String pan, @Valid @RequestBody BalanceRequest request) {
        log.debug("REST request to deposit {} into Uzcard CardAccount : {}", request.amount(), maskPan(pan));
        return ResponseEntity.ok(cardAccountService.deposit(pan, request.amount()));
    }

    @PostMapping("/{pan}/withdraw")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<CardAccountResponse> withdraw(@PathVariable String pan, @Valid @RequestBody BalanceRequest request) {
        log.debug("REST request to withdraw {} from Uzcard CardAccount : {}", request.amount(), maskPan(pan));
        return ResponseEntity.ok(cardAccountService.withdraw(pan, request.amount()));
    }

    @DeleteMapping("/{pan}")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void close(@PathVariable String pan) {
        log.debug("REST request to close Uzcard CardAccount : {}", maskPan(pan));
        cardAccountService.close(pan);
    }
}
