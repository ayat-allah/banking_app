package com.banking.auth.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AuthLoggingAspect {

    // AOP Requirement 1.6: Log all login attempts (success/failure)
    @Around("execution(* com.banking.auth.service.AuthService.login(..))")
    public Object logLoginAttempt(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String email = args.length > 0 ? extractEmail(args[0]) : "unknown";

        log.info("[AOP-AUTH] Login attempt for email: {} at {}", email, LocalDateTime.now());
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("[AOP-AUTH] Login SUCCESS for email: {} | duration: {}ms", email, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.warn("[AOP-AUTH] Login FAILED for email: {} | reason: {} | duration: {}ms",
                    email, e.getMessage(), duration);
            throw e;
        }
    }

    // AOP: Log all registration attempts
    @Around("execution(* com.banking.auth.service.AuthService.register(..))")
    public Object logRegistration(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[AOP-AUTH] Registration attempt at {}", LocalDateTime.now());
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.info("[AOP-AUTH] Registration SUCCESS | duration: {}ms",
                    System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.warn("[AOP-AUTH] Registration FAILED | reason: {} | duration: {}ms",
                    e.getMessage(), System.currentTimeMillis() - start);
            throw e;
        }
    }

    private String extractEmail(Object arg) {
        try {
            return (String) arg.getClass().getMethod("getEmail").invoke(arg);
        } catch (Exception e) {
            return arg.toString();
        }
    }
}
