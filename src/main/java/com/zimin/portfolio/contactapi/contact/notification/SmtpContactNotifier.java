package com.zimin.portfolio.contactapi.contact.notification;

import com.zimin.portfolio.contactapi.contact.application.ContactNotifier;
import com.zimin.portfolio.contactapi.contact.domain.ContactSubmission;
import com.zimin.portfolio.contactapi.error.NotificationDeliveryException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
public class SmtpContactNotifier implements ContactNotifier {

    private final JavaMailSender mailSender;
    private final String from;
    private final String to;
    private final String subjectPrefix;

    public SmtpContactNotifier(
            JavaMailSender mailSender,
            @Value("${app.mail.from:}") String from,
            @Value("${app.mail.to:}") String to,
            @Value("${app.mail.subject-prefix:[Contact Form]}") String subjectPrefix
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.to = to;
        this.subjectPrefix = subjectPrefix;
    }

    @PostConstruct
    void validateConfiguration() {
        if (from.isBlank() || to.isBlank()) {
            throw new IllegalStateException("CONTACT_MAIL_FROM and CONTACT_MAIL_TO are required when email is enabled");
        }
    }

    @Override
    public void notifyOwner(ContactSubmission submission) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(to);
        mail.setReplyTo(submission.getEmail());
        mail.setSubject(subjectPrefix + " New submission #" + submission.getId());
        mail.setText(buildBody(submission));

        try {
            mailSender.send(mail);
        } catch (MailException exception) {
            throw new NotificationDeliveryException("The email notification could not be delivered", exception);
        }
    }

    private static String buildBody(ContactSubmission submission) {
        return """
                A new contact form submission was received.

                Name: %s
                Email: %s
                Phone: %s

                Message:
                %s

                Submission ID: %d
                Received at: %s
                """.formatted(
                submission.getName(),
                submission.getEmail(),
                submission.getPhone() == null ? "Not provided" : submission.getPhone(),
                submission.getMessage(),
                submission.getId(),
                submission.getCreatedAt()
        );
    }
}
