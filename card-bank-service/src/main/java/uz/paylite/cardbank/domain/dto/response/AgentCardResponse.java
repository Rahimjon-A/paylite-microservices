package uz.paylite.cardbank.domain.dto.response;

public record AgentCardResponse(
    Long id,
    String pan,
    String type,
    String status
) {}
