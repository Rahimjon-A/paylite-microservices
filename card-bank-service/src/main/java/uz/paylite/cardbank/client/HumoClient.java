package uz.paylite.cardbank.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import uz.paylite.cardbank.config.FeignSecurityConfig;
import uz.paylite.cardbank.config.HumoFeignConfig;
import uz.paylite.cardbank.domain.dto.request.BalanceRequest;
import uz.paylite.cardbank.domain.dto.request.CreateCardAccountRequest;
import uz.paylite.cardbank.domain.dto.response.CardAccountResponse;

@FeignClient(
    name = "humo-service",
    configuration = {
        HumoFeignConfig.class,
        FeignSecurityConfig.class
    }
)
public interface HumoClient {

    @PostMapping(
        value = "/api/humo/card-accounts",
        consumes = MediaType.APPLICATION_XML_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE
    )
    CardAccountResponse createAccount(
        @RequestBody CreateCardAccountRequest request
    );

    @GetMapping(
        value = "/api/humo/card-accounts/{pan}",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    CardAccountResponse getAccount(
        @PathVariable("pan") String pan
    );

    @PostMapping(
        value = "/api/humo/card-accounts/{pan}/deposit",
        consumes = MediaType.APPLICATION_XML_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE
    )
    CardAccountResponse deposit(
        @PathVariable("pan") String pan,
        @RequestBody BalanceRequest request
    );

    @PostMapping(
        value = "/api/humo/card-accounts/{pan}/withdraw",
        consumes = MediaType.APPLICATION_XML_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE
    )
    CardAccountResponse withdraw(
        @PathVariable("pan") String pan,
        @RequestBody BalanceRequest request
    );

    @DeleteMapping(
        value = "/api/humo/card-accounts/{pan}"
    )
    void closeAccount(
        @PathVariable("pan") String pan
    );
}
