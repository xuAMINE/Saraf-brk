package com.saraf.service.recipient;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipient")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;

    @GetMapping
    public ResponseEntity<List<Recipient>> getRecipients() {
        List<Recipient> recipients = recipientService.getAllRecipients();
        return ResponseEntity.ok(recipients);
    }

    @PostMapping("/add")
    public ResponseEntity<Recipient> addRecipient(@RequestBody @Valid RecipientRequest request) {
        Recipient recipient = recipientService.addRecipient(request);
        return ResponseEntity.ok(recipient);
        // Catch UsernameNotFoundException
    }

    @PutMapping("/{ccp}")
    public ResponseEntity<Recipient> editRecipient(@PathVariable String ccp, @RequestBody @Valid EditRecipientRequest request) {
        Recipient recipient = recipientService.editRecipient(ccp, request);
        return ResponseEntity.ok(recipient);
    }

    @GetMapping("/current-user")
    public List<Recipient> getRecipientsForCurrentUser() {
        return recipientService.getRecipientsForCurrentUser();
    }

    @PutMapping("/deactivate/{ccp}")
    public ResponseEntity<Void> deactivateRecipientByCcp(@PathVariable String ccp) {
        recipientService.deactivateRecipient(ccp);
        return ResponseEntity.noContent().build();
    }

}
