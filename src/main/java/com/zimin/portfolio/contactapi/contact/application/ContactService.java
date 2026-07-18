package com.zimin.portfolio.contactapi.contact.application;

import com.zimin.portfolio.contactapi.contact.api.ContactRequest;
import com.zimin.portfolio.contactapi.contact.api.ContactResponse;
import com.zimin.portfolio.contactapi.contact.domain.ContactSubmission;
import com.zimin.portfolio.contactapi.contact.persistence.ContactSubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ContactService {

    private final ContactSubmissionRepository repository;
    private final ContactNotifier notifier;
    private final Clock clock;

    public ContactService(ContactSubmissionRepository repository, ContactNotifier notifier, Clock clock) {
        this.repository = repository;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Transactional
    public ContactResponse submit(ContactRequest request) {
        Instant now = clock.instant();
        ContactSubmission submission = new ContactSubmission(
                request.name().trim(),
                request.email().trim().toLowerCase(),
                normalizeOptional(request.phone()),
                request.message().trim(),
                now
        );

        ContactSubmission saved = repository.saveAndFlush(submission);
        notifier.notifyOwner(saved);
        return new ContactResponse(saved.getId(), "accepted", saved.getCreatedAt());
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
