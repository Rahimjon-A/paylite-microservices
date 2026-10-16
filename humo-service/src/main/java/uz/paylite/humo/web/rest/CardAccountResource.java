package uz.paylite.humo.web.rest;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.paylite.humo.domain.dto.request.BalanceRequest;
import uz.paylite.humo.domain.dto.request.CreateCardAccountRequest;
import uz.paylite.humo.domain.dto.response.CardAccountResponse;
import uz.paylite.humo.service.CardAccountService;
import uz.paylite.humo.util.CardUtil;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping(
    value = "/api/humo/card-accounts",
    produces = MediaType.APPLICATION_XML_VALUE)
@Transactional
public class CardAccountResource {

    private static final Logger log = LoggerFactory.getLogger(CardAccountResource.class);
    private final CardAccountService cardAccountService;

    public CardAccountResource(CardAccountService cardAccountService) {
        this.cardAccountService = cardAccountService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<CardAccountResponse> create(@Valid @RequestBody CreateCardAccountRequest request)
        throws URISyntaxException {
        log.debug("REST request to create Humo CardAccount for PAN ending in : {}", CardUtil.maskPan(request.pan()));

        CardAccountResponse response = cardAccountService.create(request);

        return ResponseEntity
            .created(new URI("/api/humo/card-accounts/" + response.pan()))
            .body(response);
    }

    @GetMapping("/{pan}")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<CardAccountResponse> getByPan(@PathVariable String pan) {
        log.debug("REST request to get Humo CardAccount : {}", CardUtil.maskPan(pan));
        return ResponseEntity.ok(cardAccountService.getByPan(pan));
    }

    @PostMapping(value = "/{pan}/deposit",
        consumes = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<CardAccountResponse> deposit(@PathVariable String pan,
                                                       @Valid @RequestBody BalanceRequest request) {
        log.debug("REST request to deposit {} into Humo CardAccount : {}", request.amount(), CardUtil.maskPan(pan));
        return ResponseEntity.ok(cardAccountService.deposit(pan, request.amount()));
    }

    @PostMapping(value = "/{pan}/withdraw",
        consumes = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<CardAccountResponse> withdraw(@PathVariable String pan,
                                                        @Valid @RequestBody BalanceRequest request) {
        log.debug("REST request to withdraw {} from Humo CardAccount : {}", request.amount(), CardUtil.maskPan(pan));
        return ResponseEntity.ok(cardAccountService.withdraw(pan, request.amount()));
    }

    @DeleteMapping("/{pan}")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<Void> close(@PathVariable String pan) {
        log.debug("REST request to close Humo CardAccount : {}", CardUtil.maskPan(pan));
        cardAccountService.close(pan);
        return ResponseEntity.noContent().build();
    }
}
