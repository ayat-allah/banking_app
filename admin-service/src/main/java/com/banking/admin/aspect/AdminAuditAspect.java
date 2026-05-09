package com.banking.admin.aspect;

import com.banking.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

// Requirement 13.2: AOP logs all admin actions (who, what, when)
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAuditAspect {

    private final AdminService adminService;

    @Around("execution(* com.banking.admin.service.AdminService.freezeUser(..))")
    public Object logFreezeAction(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        String adminId    = (String)  args[0];
        String adminEmail = (String)  args[1];
        String targetId   = (String)  args[2];
        boolean freeze    = (boolean) args[3];

        log.info("[ADMIN-AOP] Admin {} is {}freezing user {}",
                adminEmail, freeze ? "" : "un", targetId);

        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long elapsed = System.currentTimeMillis() - start;

        log.info("[ADMIN-AOP] Action completed in {}ms", elapsed);
        return result;
    }

    // Log execution time for any admin service method
    @Around("execution(* com.banking.admin.service.AdminService.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long elapsed = System.currentTimeMillis() - start;
        log.info("[ADMIN-AOP] {} executed in {}ms", pjp.getSignature().getName(), elapsed);
        return result;
    }
}
