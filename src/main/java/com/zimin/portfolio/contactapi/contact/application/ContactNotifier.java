package com.zimin.portfolio.contactapi.contact.application;

import com.zimin.portfolio.contactapi.contact.domain.ContactSubmission;

public interface ContactNotifier {

    void notifyOwner(ContactSubmission submission);
}
