package uz.paylite.humo.domain.dto.response;


import uz.paylite.humo.domain.enumeration.CardAccountStatus;

public record CardAccountResponse(
    String pan,
    Long balance,
    CardAccountStatus status
) {}
