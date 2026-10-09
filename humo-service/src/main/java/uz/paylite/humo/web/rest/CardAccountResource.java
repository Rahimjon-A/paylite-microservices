package uz.paylite.humo.web.rest;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.paylite.humo.domain.dto.request.BalanceRequest;
import uz.paylite.humo.domain.dto.request.CreateCardAccountRequest;
import uz.paylite.humo.domain.dto.response.CardAccountResponse;
import uz.paylite.humo.service.CardAccountService;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping(
    value = "/api/humo/card-accounts",
    produces = MediaType.APPLICATION_XML_VALUE)
@Transactional
public class CardAccountResource {

    private final CardAccountService cardAccountService;

    public CardAccountResource(CardAccountService cardAccountService) {
        this.cardAccountService = cardAccountService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<CardAccountResponse> create(@Valid @RequestBody CreateCardAccountRequest request)
        throws URISyntaxException {
        CardAccountResponse response = cardAccountService.create(request);

        return ResponseEntity
            .created(new URI("/api/humo/card-accounts/" + response.pan()))
            .body(response);
    }

    @GetMapping("/{pan}")
    public ResponseEntity<CardAccountResponse> getByPan(@PathVariable String pan) {
        return ResponseEntity.ok(cardAccountService.getByPan(pan));
    }

    @PostMapping(value = "/{pan}/deposit",
        consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<CardAccountResponse> deposit(@PathVariable String pan,
                                                       @Valid @RequestBody BalanceRequest request) {
        return ResponseEntity.ok(cardAccountService.deposit(pan, request.amount()));
    }

    @PostMapping(value = "/{pan}/withdraw",
        consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<CardAccountResponse> withdraw(@PathVariable String pan,
                                                        @Valid @RequestBody BalanceRequest request) {
        return ResponseEntity.ok(cardAccountService.withdraw(pan, request.amount()));
    }

    @DeleteMapping("/{pan}")
    public ResponseEntity<Void> close(@PathVariable String pan) {
        cardAccountService.close(pan);
        return ResponseEntity.noContent().build();
    }
}
