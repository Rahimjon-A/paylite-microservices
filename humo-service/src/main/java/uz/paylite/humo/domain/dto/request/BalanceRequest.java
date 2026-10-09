package uz.paylite.humo.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BalanceRequest(
    @NotNull
    @Positive
    Long amount
) {}
