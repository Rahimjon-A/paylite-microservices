package uz.paylite.uzcard.domain.dto.response;

import uz.paylite.uzcard.domain.enumeration.CardAccountStatus;

public record CardAccountResponse(
    String pan,
    Long balance,
    CardAccountStatus status
) {}
