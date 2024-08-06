package com.saraf.service.transfer;

import com.saraf.security.exception.InvalidTransferCredException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/add")
    public ResponseEntity<Transfer> add(@RequestBody TransferRequest request) {

        Transfer transfer = transferService.addTransfer(request);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping
    public ResponseEntity<List<TransferDTO>> getTransfersForUser() {
        List<TransferDTO> transfers = transferService.getTransfersForUser();
        return ResponseEntity.ok(transfers);
    }

    @PostMapping("/check-transfer-credentials")
    public ResponseEntity<String> checkTransferCredentials(@RequestBody @Valid TransferRequest request) {
        return ResponseEntity.ok("ok");
    }
}
