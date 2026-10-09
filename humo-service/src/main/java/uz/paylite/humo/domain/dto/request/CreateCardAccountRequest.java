package uz.paylite.humo.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCardAccountRequest(@NotBlank
                                   @Size(min = 16, max = 16)
                                   @Pattern(regexp = "\\d{16}")
                                   String pan) {}
