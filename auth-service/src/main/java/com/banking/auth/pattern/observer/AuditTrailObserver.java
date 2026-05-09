package com.banking.auth.pattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DESIGN PATTERN: Observer Pattern — Concrete Observer 2
 *
 * Reacts to account events by writing a structured audit trail.
 * In a real system this would persist to an audit_log table.
 * Particularly important for: ACCOUNT_FROZEN, ACCOUNT_UNFROZEN (admin actions).
 */
@Component
@Slf4j
public class AuditTrailObserver implements AccountEventListener {

    @Override
    public void onEvent(AccountEvent event) {
        // In production: persist to audit_log table
        // Here we log in structured format for demo
        String auditEntry = buildAuditEntry(event);
        log.info("[AUDIT-TRAIL] {}", auditEntry);
    }

    private String buildAuditEntry(AccountEvent event) {
        return String.format(
                "EVENT=%s | USER=%s | EMAIL=%s | DETAILS=%s | TIME=%s",
                event.getType().name(),
                event.getUserId() != null ? event.getUserId() : "N/A",
                event.getUserEmail() != null ? event.getUserEmail() : "N/A",
                event.getDetails(),
                event.getOccurredAt()
        );
    }

    @Override
    public String getObserverName() {
        return "AuditTrailObserver";
    }
}
