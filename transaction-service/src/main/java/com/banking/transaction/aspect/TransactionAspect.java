package com.banking.transaction.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class TransactionAspect {

    // Requirement 3.5: AOP measures execution time of transfer method
    @Around("execution(* com.banking.transaction.service.TransactionService.transfer(..))")
    public Object measureTransferTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("[AOP-TXN] Transfer started at {}", LocalDateTime.now());

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("[AOP-TXN] Transfer completed successfully | execution time: {}ms", duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("[AOP-TXN] Transfer FAILED | reason: {} | execution time: {}ms",
                    e.getMessage(), duration);
            throw e;
        }
    }

    // AOP for wallet operations
    @Around("execution(* com.banking.transaction.service.TransactionService.*(..)) && !execution(* com.banking.transaction.service.TransactionService.transfer(..))")
    public Object logAllServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();
        log.info("[AOP-TXN] Method '{}' called at {}", method, LocalDateTime.now());

        try {
            Object result = joinPoint.proceed();
            log.info("[AOP-TXN] Method '{}' done | {}ms", method,
                    System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("[AOP-TXN] Method '{}' failed: {} | {}ms", method,
                    e.getMessage(), System.currentTimeMillis() - start);
            throw e;
        }
    }
}
