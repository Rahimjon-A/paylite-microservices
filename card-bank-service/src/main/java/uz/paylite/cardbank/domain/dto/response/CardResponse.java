package uz.paylite.cardbank.domain.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import uz.paylite.cardbank.domain.enumeration.CardStatus;
import uz.paylite.cardbank.domain.enumeration.CardType;

public record CardResponse(
    Long id,
    String pan,
    CardType type,
    LocalDate expireDate,
    String fullName,
    String phoneNumber,
    CardStatus status,
    Instant createdAt
) {}
