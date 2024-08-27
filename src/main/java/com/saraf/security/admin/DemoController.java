package com.saraf.security.admin;

import com.saraf.security.auditing.ApplicationAuditAware;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo-controller")
@Hidden
@RequiredArgsConstructor
public class DemoController {




    @GetMapping
  public ResponseEntity<String> sayHello() {
    return ResponseEntity.ok("Hello from secured endpoint");
  }


}
