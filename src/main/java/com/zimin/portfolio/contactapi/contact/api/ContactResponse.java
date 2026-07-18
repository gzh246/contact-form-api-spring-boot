package com.zimin.portfolio.contactapi.contact.api;

import java.time.Instant;

public record ContactResponse(
        long id,
        String status,
        Instant submittedAt
) {
}
