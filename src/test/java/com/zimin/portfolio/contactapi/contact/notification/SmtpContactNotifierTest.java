package com.zimin.portfolio.contactapi.contact.notification;

import com.zimin.portfolio.contactapi.contact.domain.ContactSubmission;
import com.zimin.portfolio.contactapi.error.NotificationDeliveryException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpContactNotifierTest {

    @Test
    void sendsPlainTextNotificationWithReplyToAddress() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        SmtpContactNotifier notifier = new SmtpContactNotifier(
                sender,
                "no-reply@example.com",
                "owner@example.com",
                "[Contact Form]"
        );
        notifier.validateConfiguration();

        ContactSubmission submission = submissionWithId(7L);
        notifier.notifyOwner(submission);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());
        SimpleMailMessage mail = captor.getValue();

        assertThat(mail.getFrom()).isEqualTo("no-reply@example.com");
        assertThat(mail.getTo()).containsExactly("owner@example.com");
        assertThat(mail.getReplyTo()).isEqualTo("demo@example.com");
        assertThat(mail.getSubject()).isEqualTo("[Contact Form] New submission #7");
        assertThat(mail.getText()).contains("Synthetic message", "Submission ID: 7");
    }

    @Test
    void translatesMailProviderFailureToDomainException() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        doThrow(new MailSendException("SMTP unavailable"))
                .when(sender).send(any(SimpleMailMessage.class));
        SmtpContactNotifier notifier = new SmtpContactNotifier(
                sender,
                "no-reply@example.com",
                "owner@example.com",
                "[Contact Form]"
        );

        assertThatThrownBy(() -> notifier.notifyOwner(submissionWithId(8L)))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessage("The email notification could not be delivered");
    }

    private static ContactSubmission submissionWithId(long id) throws Exception {
        ContactSubmission submission = new ContactSubmission(
                "Demo User",
                "demo@example.com",
                "+86 138 0000 0000",
                "Synthetic message",
                Instant.parse("2026-07-16T00:00:00Z")
        );
        var idField = ContactSubmission.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(submission, id);
        return submission;
    }
}
