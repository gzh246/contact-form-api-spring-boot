package com.zimin.portfolio.contactapi.contact.notification;

import com.zimin.portfolio.contactapi.contact.application.ContactNotifier;
import com.zimin.portfolio.contactapi.contact.domain.ContactSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingContactNotifier implements ContactNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingContactNotifier.class);

    @Override
    public void notifyOwner(ContactSubmission submission) {
        log.info("Demo notification accepted for contact submission id={} at={}",
                submission.getId(), submission.getCreatedAt());
    }
}
