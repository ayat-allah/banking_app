package com.banking.auth.pattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DESIGN PATTERN: Observer Pattern — Subject
 *
 * AccountEventPublisher maintains a list of observers and
 * notifies all of them when an account event is published.
 *
 * Spring auto-injects all beans implementing AccountEventListener,
 * so adding a new observer only requires creating a new
 * @Component class — no changes needed here (Open/Closed Principle).
 */
@Component
@Slf4j
public class AccountEventPublisher {

    private final List<AccountEventListener> observers;

    /**
     * Spring injects all AccountEventListener implementations automatically.
     * Currently: SecurityLogObserver and AuditTrailObserver.
     */
    public AccountEventPublisher(List<AccountEventListener> observers) {
        this.observers = new ArrayList<>(observers);
        log.info("[OBSERVER-PUBLISHER] Registered {} observer(s): {}",
                observers.size(),
                observers.stream()
                        .map(AccountEventListener::getObserverName)
                        .toList());
    }

    /**
     * Publish an event to all registered observers.
     */
    public void publish(AccountEvent event) {
        log.debug("[OBSERVER-PUBLISHER] Publishing event: {}", event.getType());
        for (AccountEventListener observer : observers) {
            try {
                observer.onEvent(event);
            } catch (Exception e) {
                // One observer failure should not affect others
                log.error("[OBSERVER-PUBLISHER] Observer {} threw exception: {}",
                        observer.getObserverName(), e.getMessage());
            }
        }
    }

    /** Register a new observer at runtime */
    public void addObserver(AccountEventListener observer) {
        observers.add(observer);
        log.info("[OBSERVER-PUBLISHER] Added observer: {}", observer.getObserverName());
    }

    /** Remove an observer */
    public void removeObserver(AccountEventListener observer) {
        observers.remove(observer);
        log.info("[OBSERVER-PUBLISHER] Removed observer: {}", observer.getObserverName());
    }
}
