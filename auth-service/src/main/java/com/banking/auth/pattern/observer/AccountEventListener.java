package com.banking.auth.pattern.observer;

/**
 * DESIGN PATTERN: Observer Pattern — Observer interface
 *
 * Intent:
 *   Define a one-to-many dependency between objects so that when one
 *   object (Subject) changes state, all its dependents (Observers)
 *   are notified and updated automatically.
 *
 * Why used here:
 *   When account events happen (login, freeze, register), multiple
 *   independent reactions need to occur:
 *     1. Security logging
 *     2. Audit trail recording
 *   Instead of the AuthService calling each reaction directly (tight coupling),
 *   it simply publishes an event and all registered observers handle it.
 *   New observers can be added without changing AuthService (Open/Closed Principle).
 *
 * Participants:
 *   - AccountEventListener (this interface)    → Observer
 *   - SecurityLogObserver                      → ConcreteObserver
 *   - AuditTrailObserver                       → ConcreteObserver
 *   - AccountEventPublisher                    → Subject
 *   - AuthService                              → triggers events
 */
public interface AccountEventListener {

    /**
     * Called when an account event is published.
     * Each observer implements its own reaction logic.
     */
    void onEvent(AccountEvent event);

    /**
     * Returns the name of this observer (used for logging).
     */
    String getObserverName();
}
