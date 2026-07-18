package com.zimin.portfolio.contactapi.contact.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 254, message = "Email must be at most 254 characters")
        String email,

        @Pattern(
                regexp = "^(?=(?:\\D*\\d){7})[+0-9()\\-\\s]{7,30}$",
                message = "Phone must contain at least 7 digits and may use spaces, parentheses, plus signs and hyphens"
        )
        String phone,

        @NotBlank(message = "Message is required")
        @Size(max = 2000, message = "Message must be at most 2000 characters")
        String message
) {

    public ContactRequest {
        name = trim(name);
        email = trim(email);
        phone = trimToNull(phone);
        message = trim(message);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }
}
