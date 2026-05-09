package com.banking.auth.pattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DESIGN PATTERN: Observer Pattern — Concrete Observer 1
 *
 * Reacts to account events by writing security logs.
 * Particularly important for: LOGIN_SUCCESS, LOGIN_FAILED
 * (satisfies Requirement 1.6: AOP logging of all login attempts)
 */
@Component
@Slf4j
public class SecurityLogObserver implements AccountEventListener {

    @Override
    public void onEvent(AccountEvent event) {
        switch (event.getType()) {

            case LOGIN_SUCCESS ->
                log.info("[SECURITY] ✅ Login SUCCESS — user={} email={} at={}",
                        event.getUserId(), event.getUserEmail(), event.getOccurredAt());

            case LOGIN_FAILED ->
                log.warn("[SECURITY] ❌ Login FAILED — email={} reason={} at={}",
                        event.getUserEmail(), event.getDetails(), event.getOccurredAt());

            case ACCOUNT_FROZEN ->
                log.warn("[SECURITY] 🔒 Account FROZEN — userId={} details={} at={}",
                        event.getUserId(), event.getDetails(), event.getOccurredAt());

            case ACCOUNT_UNFROZEN ->
                log.info("[SECURITY] 🔓 Account UNFROZEN — userId={} details={} at={}",
                        event.getUserId(), event.getDetails(), event.getOccurredAt());

            case ACCOUNT_REGISTERED ->
                log.info("[SECURITY] 🆕 New REGISTRATION — userId={} email={} at={}",
                        event.getUserId(), event.getUserEmail(), event.getOccurredAt());
        }
    }

    @Override
    public String getObserverName() {
        return "SecurityLogObserver";
    }
}
