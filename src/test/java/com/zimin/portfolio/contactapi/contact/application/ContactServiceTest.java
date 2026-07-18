package com.zimin.portfolio.contactapi.contact.application;

import com.zimin.portfolio.contactapi.contact.api.ContactRequest;
import com.zimin.portfolio.contactapi.contact.domain.ContactSubmission;
import com.zimin.portfolio.contactapi.contact.persistence.ContactSubmissionRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContactServiceTest {

    @Test
    void normalizesInputAndNotifiesOwner() {
        ContactSubmissionRepository repository = mock(ContactSubmissionRepository.class);
        ContactNotifier notifier = mock(ContactNotifier.class);
        Instant now = Instant.parse("2026-07-16T01:02:03Z");
        ContactService service = new ContactService(
                repository,
                notifier,
                Clock.fixed(now, ZoneOffset.UTC)
        );

        when(repository.saveAndFlush(any(ContactSubmission.class))).thenAnswer(invocation -> {
            ContactSubmission submission = invocation.getArgument(0);
            var id = ContactSubmission.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(submission, 42L);
            return submission;
        });

        var response = service.submit(new ContactRequest(
                "  Demo User  ",
                "  DEMO@EXAMPLE.COM ",
                "  +86 138 0000 0000  ",
                "  Hello from the test.  "
        ));

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.submittedAt()).isEqualTo(now);
        verify(notifier).notifyOwner(any(ContactSubmission.class));
    }
}
