package uz.paylite.cardbank.domain.dto.response;

public record CardAccountResponse(
    String pan,
    Long balance,
    String status
) {}
