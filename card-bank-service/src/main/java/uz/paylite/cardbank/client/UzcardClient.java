package uz.paylite.cardbank.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import uz.paylite.cardbank.config.FeignSecurityConfig;
import uz.paylite.cardbank.domain.dto.request.BalanceRequest;
import uz.paylite.cardbank.domain.dto.request.CreateCardAccountRequest;
import uz.paylite.cardbank.domain.dto.response.CardAccountResponse;

@FeignClient(name = "uzcard-service", configuration = FeignSecurityConfig.class)
public interface UzcardClient {

    @PostMapping("/api/uzcard/card-accounts")
    CardAccountResponse createAccount(@RequestBody CreateCardAccountRequest request);

    @GetMapping("/api/uzcard/card-accounts/{pan}")
    CardAccountResponse getAccount(@PathVariable("pan") String pan);

    @PostMapping("/api/uzcard/card-accounts/{pan}/deposit")
    CardAccountResponse deposit(@PathVariable("pan") String pan, @RequestBody BalanceRequest request);

    @PostMapping("/api/uzcard/card-accounts/{pan}/withdraw")
    CardAccountResponse withdraw(@PathVariable("pan") String pan, @RequestBody BalanceRequest request);

    @DeleteMapping("/api/uzcard/card-accounts/{pan}")
    void closeAccount(@PathVariable("pan") String pan);
}
