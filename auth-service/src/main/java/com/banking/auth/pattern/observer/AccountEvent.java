package com.banking.auth.pattern.observer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DESIGN PATTERN: Observer Pattern — Event object
 *
 * Represents an account event that observers react to.
 * Examples: ACCOUNT_FROZEN, ACCOUNT_UNFROZEN, LOGIN_FAILED, LOGIN_SUCCESS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEvent {

    public enum EventType {
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        ACCOUNT_REGISTERED,
        ACCOUNT_FROZEN,
        ACCOUNT_UNFROZEN
    }

    private EventType type;
    private String userId;
    private String userEmail;
    private String details;

    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    // ─── Static factory helpers ───

    public static AccountEvent loginSuccess(String userId, String email) {
        return AccountEvent.builder()
                .type(EventType.LOGIN_SUCCESS)
                .userId(userId)
                .userEmail(email)
                .details("Successful login")
                .build();
    }

    public static AccountEvent loginFailed(String email, String reason) {
        return AccountEvent.builder()
                .type(EventType.LOGIN_FAILED)
                .userEmail(email)
                .details("Login failed: " + reason)
                .build();
    }

    public static AccountEvent registered(String userId, String email) {
        return AccountEvent.builder()
                .type(EventType.ACCOUNT_REGISTERED)
                .userId(userId)
                .userEmail(email)
                .details("New account registered")
                .build();
    }

    public static AccountEvent frozen(String userId, String frozenBy) {
        return AccountEvent.builder()
                .type(EventType.ACCOUNT_FROZEN)
                .userId(userId)
                .details("Account frozen by admin: " + frozenBy)
                .build();
    }

    public static AccountEvent unfrozen(String userId, String unfrozenBy) {
        return AccountEvent.builder()
                .type(EventType.ACCOUNT_UNFROZEN)
                .userId(userId)
                .details("Account unfrozen by admin: " + unfrozenBy)
                .build();
    }
}
