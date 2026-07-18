package com.zimin.portfolio.contactapi.contact.api;

import com.zimin.portfolio.contactapi.contact.application.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contacts")
@Tag(name = "Contact submissions")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Submit a contact form",
            description = "Validates and stores a contact request, then sends an email notification when SMTP is enabled.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Submission accepted"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation or JSON error",
                            content = @Content(schema = @Schema(implementation = com.zimin.portfolio.contactapi.error.ApiErrorResponse.class))
                    ),
                    @ApiResponse(responseCode = "502", description = "The configured email provider could not deliver the notification")
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "name": "Demo User",
                      "email": "demo@example.com",
                      "phone": "+86 138 0000 0000",
                      "message": "I would like to discuss a small Spring Boot project."
                    }
                    """))
    )
    public ContactResponse submit(@Valid @RequestBody ContactRequest request) {
        return contactService.submit(request);
    }
}
