package com.saraf.service.transfer;

import com.saraf.security.admin.s3.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final S3Service s3Service;

    @PostMapping("/add")
    public ResponseEntity<Transfer> add(@RequestBody TransferRequest request) {
        Transfer transfer = transferService.addTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transfer);
    }

    @GetMapping
    public Page<TransferDTO> getTransfers(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return transferService.getTransfersForUser(page, size);
    }

    @GetMapping("/NonCancelled")
    public Page<TransferDTO> getNonCancelled(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return transferService.getNonCancelledTransfersForUser(page, size);
    }

    @PostMapping("/check-transfer-credentials")
    public ResponseEntity<String> checkTransferCredentials(@RequestBody @Valid TransferRequest request) {
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/receipt/{transferId}")
    public ResponseEntity<String> getFileUrl(@PathVariable Integer transferId) {
        String fileName = transferService.getReceiptName(transferId);
        String url = s3Service.generatePreSignedUrl(fileName, 10);
        return ResponseEntity.ok(url);
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Transfer> cancel(@PathVariable Integer id) {
        Transfer canceledTransfer = transferService.cancelTransfer(id);
        return ResponseEntity.ok(canceledTransfer);
    }

    @GetMapping("/user-phone/{transferId}")
    public ResponseEntity<String> getUserPhone(@PathVariable Integer transferId) {
        String phoneNumber = transferService.getUserPhoneNumberByTransferId(transferId);
        if (phoneNumber == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(phoneNumber);
    }

}
