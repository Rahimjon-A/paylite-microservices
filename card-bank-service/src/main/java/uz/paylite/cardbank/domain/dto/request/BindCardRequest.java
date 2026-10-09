package uz.paylite.cardbank.domain.dto.request;

import java.time.Instant;
import java.time.LocalDate;

public record BindCardRequest(
    String pan,
    String type,
    LocalDate expireDate,
    String status,
    Instant createdAt
) {}
