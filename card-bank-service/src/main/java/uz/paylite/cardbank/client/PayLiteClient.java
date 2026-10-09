package uz.paylite.cardbank.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uz.paylite.cardbank.config.FeignSecurityConfig;
import uz.paylite.cardbank.domain.dto.request.BindCardRequest;

@FeignClient(
    name = "paylite-service",
    configuration = FeignSecurityConfig.class
)
public interface PayLiteClient {

    @PostMapping("/api/agent-cards/bind")
    void bindCard(@RequestBody BindCardRequest request);
}
