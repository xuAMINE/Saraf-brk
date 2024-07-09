package com.saraf.security.demo;

import com.saraf.security.user.Role;
import com.saraf.security.user.RoleService;
import com.saraf.security.user.RoleUpdateRequest;
import com.saraf.security.user.UserNotFoundException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final RoleService roleService;

    public AdminController(RoleService roleService) {
        this.roleService = roleService;
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
