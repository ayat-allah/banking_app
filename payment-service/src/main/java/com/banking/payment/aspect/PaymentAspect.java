package com.banking.payment.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class PaymentAspect {

    private static final int MAX_RETRIES = 3;

    // Requirement 7.5 / 8.5: AOP measures execution time of transfer method
    @Around("execution(* com.banking.payment.service.PaymentService.deposit(..))" +
            " || execution(* com.banking.payment.service.PaymentService.withdraw(..))")
    public Object measureAndRetryExternalTransfer(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        log.info("[AOP-PAY] External transfer '{}' started at {}", method, LocalDateTime.now());

        long start = System.currentTimeMillis();
        int attempt = 0;
        Throwable lastException = null;

        // System Evolution a: AOP retries failed external transfers (max 3 times)
        while (attempt < MAX_RETRIES) {
            attempt++;
            try {
                Object result = joinPoint.proceed();
                log.info("[AOP-PAY] '{}' SUCCESS on attempt {} | {}ms",
                        method, attempt, System.currentTimeMillis() - start);
                return result;
            } catch (Exception e) {
                lastException = e;
                log.warn("[AOP-PAY] '{}' FAILED attempt {}/{} | reason: {}",
                        method, attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    Thread.sleep(500L * attempt); // exponential-ish backoff
                }
            }
        }

        log.error("[AOP-PAY] '{}' FAILED after {} attempts | total time: {}ms",
                method, MAX_RETRIES, System.currentTimeMillis() - start);
        throw lastException;
    }

    // Log external payments to non-registered users
    @Around("execution(* com.banking.payment.service.PaymentService.sendToExternal(..))")
    public Object logExternalPayment(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("[AOP-PAY] External payment started at {}", LocalDateTime.now());
        try {
            Object result = joinPoint.proceed();
            log.info("[AOP-PAY] External payment SUCCESS | {}ms",
                    System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("[AOP-PAY] External payment FAILED: {} | {}ms",
                    e.getMessage(), System.currentTimeMillis() - start);
            throw e;
        }
    }
}
