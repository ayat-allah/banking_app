package com.banking.transaction.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthServiceClient {

    @GetMapping("/users/{userId}")
    UserInfoResponse getUserById(@PathVariable String userId);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class UserInfoResponse {
        private String id;
        private String name;
        private String email;
        private String phoneNumber;
        private String role;
        private boolean frozen;
        private boolean active;
    }
}
