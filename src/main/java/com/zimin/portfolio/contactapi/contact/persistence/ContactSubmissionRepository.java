package com.zimin.portfolio.contactapi.contact.persistence;

import com.zimin.portfolio.contactapi.contact.domain.ContactSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactSubmissionRepository extends JpaRepository<ContactSubmission, Long> {
}
