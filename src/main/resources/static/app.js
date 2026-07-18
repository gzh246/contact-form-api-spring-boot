const form = document.querySelector("#contact-form");
const sampleButton = document.querySelector("#sample-button");
const submitButton = document.querySelector("#submit-button");
const buttonLabel = submitButton.querySelector(".button-label");
const statusBox = document.querySelector("#form-status");
const messageInput = document.querySelector("#message");
const messageCount = document.querySelector("#message-count");

const fields = ["name", "email", "phone", "message"];
const phonePattern = /^(?=(?:\D*\d){7})[+0-9()\-\s]{7,30}$/;

function setFieldError(field, message = "") {
    const input = document.querySelector(`#${field}`);
    const error = document.querySelector(`#${field}-error`);
    error.textContent = message;
    input.setAttribute("aria-invalid", message ? "true" : "false");
}

function clearErrors() {
    fields.forEach((field) => setFieldError(field));
}

function showStatus(message, isError = false) {
    statusBox.textContent = message;
    statusBox.classList.toggle("is-error", isError);
    statusBox.hidden = false;
}

function hideStatus() {
    statusBox.hidden = true;
    statusBox.textContent = "";
    statusBox.classList.remove("is-error");
}

function validate(payload) {
    let valid = true;

    if (!payload.name) {
        setFieldError("name", "Name is required");
        valid = false;
    }

    if (!payload.email) {
        setFieldError("email", "Email is required");
        valid = false;
    } else if (!document.querySelector("#email").checkValidity()) {
        setFieldError("email", "Enter a valid email address");
        valid = false;
    }

    if (payload.phone && !phonePattern.test(payload.phone)) {
        setFieldError("phone", "Use 7-30 digits and common phone symbols");
        valid = false;
    }

    if (!payload.message) {
        setFieldError("message", "Message is required");
        valid = false;
    }

    return valid;
}

function payloadFromForm() {
    return {
        name: form.elements.name.value.trim(),
        email: form.elements.email.value.trim(),
        phone: form.elements.phone.value.trim() || null,
        message: form.elements.message.value.trim()
    };
}

function setSubmitting(submitting) {
    submitButton.disabled = submitting;
    sampleButton.disabled = submitting;
    buttonLabel.textContent = submitting ? "Submitting..." : "Submit demo request";
}

sampleButton.addEventListener("click", () => {
    form.elements.name.value = "Alex Morgan";
    form.elements.email.value = "alex.morgan@example.com";
    form.elements.phone.value = "+1 415 555 0136";
    form.elements.message.value = "I would like to discuss a small Spring Boot API integration.";
    messageCount.textContent = `${form.elements.message.value.length} / 2000`;
    clearErrors();
    hideStatus();
    form.elements.name.focus();
});

messageInput.addEventListener("input", () => {
    messageCount.textContent = `${messageInput.value.length} / 2000`;
});

fields.forEach((field) => {
    document.querySelector(`#${field}`).addEventListener("input", () => setFieldError(field));
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearErrors();
    hideStatus();

    const payload = payloadFromForm();
    if (!validate(payload)) {
        showStatus("Please correct the highlighted fields before submitting.", true);
        const firstInvalid = form.querySelector('[aria-invalid="true"]');
        firstInvalid?.focus();
        return;
    }

    setSubmitting(true);
    try {
        const response = await fetch("api/v1/contacts", {
            method: "POST",
            headers: {"Content-Type": "application/json", "Accept": "application/json"},
            body: JSON.stringify(payload)
        });

        const contentType = response.headers.get("content-type") || "";
        const result = contentType.includes("application/json") ? await response.json() : null;

        if (!response.ok) {
            if (result?.fieldErrors) {
                Object.entries(result.fieldErrors).forEach(([field, message]) => {
                    if (fields.includes(field)) setFieldError(field, message);
                });
            }
            const message = response.status === 429
                ? "This demo is rate-limited. Please wait a moment and try again."
                : result?.message || `The API returned HTTP ${response.status}.`;
            showStatus(message, true);
            form.querySelector('[aria-invalid="true"]')?.focus();
            return;
        }

        showStatus(`Submission #${result.id} was accepted and stored. The server completed its configured notification step.`);
        form.reset();
        messageCount.textContent = "0 / 2000";
    } catch {
        showStatus("The demo service could not be reached. Please try again shortly.", true);
    } finally {
        setSubmitting(false);
    }
});
