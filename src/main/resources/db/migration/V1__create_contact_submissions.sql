CREATE TABLE contact_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(30) NULL,
    message VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_contact_submissions_created_at
    ON contact_submissions (created_at);
