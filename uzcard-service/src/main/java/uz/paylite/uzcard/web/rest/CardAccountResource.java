package uz.paylite.uzcard.web.rest;

import jakarta.validation.Valid;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.paylite.uzcard.domain.dto.request.BalanceRequest;
import uz.paylite.uzcard.domain.dto.request.CreateCardAccountRequest;
import uz.paylite.uzcard.domain.dto.response.CardAccountResponse;
import uz.paylite.uzcard.service.CardAccountService;

@RestController
@RequestMapping("/api/uzcard/card-accounts")
public class CardAccountResource {

    private final CardAccountService cardAccountService;

    public CardAccountResource(CardAccountService cardAccountService) {
        this.cardAccountService = cardAccountService;
    }

    @PostMapping
    public ResponseEntity<CardAccountResponse> create(@Valid @RequestBody CreateCardAccountRequest request) throws URISyntaxException {
        CardAccountResponse response = cardAccountService.create(request);

        return ResponseEntity.created(new URI("/api/uzcard/card-accounts/" + response.pan())).body(response);
    }

    @GetMapping("/{pan}")
    public ResponseEntity<CardAccountResponse> getByPan(@PathVariable String pan) {
        return ResponseEntity.ok(cardAccountService.getByPan(pan));
    }

    @PostMapping("/{pan}/deposit")
    public ResponseEntity<CardAccountResponse> deposit(@PathVariable String pan, @Valid @RequestBody BalanceRequest request) {
        return ResponseEntity.ok(cardAccountService.deposit(pan, request.amount()));
    }

    @PostMapping("/{pan}/withdraw")
    public ResponseEntity<CardAccountResponse> withdraw(@PathVariable String pan, @Valid @RequestBody BalanceRequest request) {
        return ResponseEntity.ok(cardAccountService.withdraw(pan, request.amount()));
    }

    @DeleteMapping("/{pan}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void close(@PathVariable String pan) {
        cardAccountService.close(pan);
    }
}
