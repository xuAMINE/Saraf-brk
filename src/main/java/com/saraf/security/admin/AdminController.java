package com.saraf.security.admin;

import com.saraf.security.admin.s3.S3Service;
import com.saraf.security.exception.TransferNotFoundException;
import com.saraf.security.user.RoleService;
import com.saraf.security.user.RoleUpdateRequest;

import com.saraf.service.transfer.*;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final RoleService roleService;
    private final TransferService transferService;
    private final S3Service s3Service;

    @GetMapping("/transfers")
    public Page<TransferAdminDTO> getTransfers(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return transferService.getTransfersForAdmin(page, size);
    }

    @GetMapping("/transfers/pending")
    public Page<TransferAdminDTO> getPendingTransfers(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return transferService.getPendingTransfersForAdmin(page, size);
    }

    @PostMapping("/upload-receipt/{id}")
    public ResponseEntity<?> uploadReceipt(@PathVariable Integer id, @RequestParam("receipt") MultipartFile receipt) {
        try {
            Transfer transfer = s3Service.saveReceipt(id, receipt);
            return ResponseEntity.ok().body(new ApiResponse(true, "Receipt uploaded successfully", transfer.getReceipt()));
        } catch (TransferNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Transfer not found"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Error uploading receipt"));
        }
    }

    @PatchMapping("/update-status")
    public ResponseEntity<Transfer> updateStatus(@RequestBody UpdateTransferStatusDTO request) {
        Transfer updatedTransfer = transferService.updateStatus(request.getId(), request.getStatus());
        return ResponseEntity.ok(updatedTransfer);
    }


    @GetMapping
    @PreAuthorize("hasAuthority('admin:read')")
    public String get() {
        return "GET:: admin controller";
    }
    @PostMapping
    @PreAuthorize("hasAuthority('admin:create')")
    @Hidden
    public String post() {
        return "POST:: admin controller";
    }
    @PutMapping
    @PreAuthorize("hasAuthority('admin:update')")
    @Hidden
    public String put() {
        return "PUT:: admin controller";
    }
    @DeleteMapping
    @PreAuthorize("hasAuthority('admin:delete')")
    @Hidden
    public String delete() {
        return "DELETE:: admin controller";
    }

    @PutMapping("/user/{userId}/role")
    @PreAuthorize("hasAuthority('admin:update')")
    @Hidden
    public ResponseEntity<?> putRole(@PathVariable Integer userId, @RequestBody RoleUpdateRequest request) {
        roleService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok("User role updated successfully");
    }
}
