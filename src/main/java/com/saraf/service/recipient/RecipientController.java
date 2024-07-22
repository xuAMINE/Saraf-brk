package com.saraf.service.recipient;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipient")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;

    @PostMapping("/add")
    public ResponseEntity<Recipient> addRecipient(@RequestBody AddRecipientRequest request) {
        Recipient recipient = recipientService.addRecipient(request);
        return ResponseEntity.ok(recipient);
    }

}
