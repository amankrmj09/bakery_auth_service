package com.blubugtech.bakery_auth_service.event;

import java.util.UUID;

public interface DomainEventPublisher {
    void publishUserRegisteredEvent(UUID userId, String email, String firstName, String lastName, String phoneNumber);

    void publishNewSignInEvent(UUID userId, String email, String firstName, String lastName, String ipAddress, String location);

    void publishPasswordChangedEvent(UUID userId, String email, String firstName, String lastName, String phoneNumber);

    void publishOtpRequestedEvent(UUID userId, String email, String firstName, String lastName, String phoneNumber, String otp);
}
