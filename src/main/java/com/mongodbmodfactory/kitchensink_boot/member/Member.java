package com.mongodbmodfactory.kitchensink_boot.member;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;

public record Member(
        @Id Long id,
        @NotNull
        @Size(min = 1, max = 25)
        @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers") String name,
        @NotNull
        @NotEmpty
        @Email String email,
        @NotNull
        @Size(min = 10, max = 12)
        @Digits(fraction = 0, integer = 12) String phoneNumber) {
}
