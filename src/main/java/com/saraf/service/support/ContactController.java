package com.saraf.service.support;

import com.saraf.security.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<?> addContact(@RequestBody @Valid ContactRequest request) throws MessagingException, UnsupportedEncodingException {
        emailService.sendContactUsEmail(request.name, request.email, request.message);
        emailService.sendContactConfirmEmail(request.name, request.email, request.message);
        return ResponseEntity.ok().body("Thank you for contacting us, " + request.name + "!");
    }
}
