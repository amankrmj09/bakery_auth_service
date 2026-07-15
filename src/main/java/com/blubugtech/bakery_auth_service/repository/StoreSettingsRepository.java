package com.blubugtech.bakery_auth_service.repository;

import com.blubugtech.bakery_auth_service.entity.StoreSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StoreSettingsRepository extends JpaRepository<StoreSettings, UUID> {
}
