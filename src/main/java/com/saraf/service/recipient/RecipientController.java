package com.saraf.service.recipient;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipient")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;

    @PostMapping("/add")
    public ResponseEntity<Recipient> addRecipient(@RequestBody @Valid RecipientRequest request) {
        Recipient recipient = recipientService.addRecipient(request);
        return ResponseEntity.ok(recipient);
        // Catch UsernameNotFoundException
    }

    @GetMapping
    public ResponseEntity<List<Recipient>> getRecipients() {
        List<Recipient> recipients = recipientService.getAllRecipients();
        return ResponseEntity.ok(recipients);
    }

    @GetMapping("/current-user")
    public List<Recipient> getRecipientsForCurrentUser() {
        return recipientService.getRecipientsForCurrentUser();
    }


}
