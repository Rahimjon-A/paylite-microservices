package uz.paylite.cardbank.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uz.paylite.cardbank.domain.enumeration.CardType;

public record CardCreateRequest(

    @NotBlank
    String fullName,

    @NotBlank
    @Size(min = 14, max = 14)
    @Pattern(regexp = "\\d{14}")
    String pinfl,

    @NotBlank
    String phoneNumber,

    @NotNull
    CardType type
) {}
