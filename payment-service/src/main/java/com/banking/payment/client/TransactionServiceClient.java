package com.banking.payment.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "transaction-service", path = "/api/wallet")
public interface TransactionServiceClient {

    @PostMapping("/credit")
    void creditWallet(@RequestBody WalletRequest request);

    @PostMapping("/debit")
    void debitWallet(@RequestBody WalletRequest request);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class WalletRequest {
        private String userId;
        private BigDecimal amount;
    }
}
