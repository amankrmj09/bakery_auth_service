package com.blubugtech.bakery_auth_service.controller;

import com.blubugtech.bakery_auth_service.entity.StoreSettings;
import com.blubugtech.bakery_auth_service.repository.StoreSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping({"/api/store/settings", "/api/store/settings/"})
@Tag(name = "Store Settings", description = "Endpoints for managing global bakery settings")
public class StoreSettingsController {
    private static final Logger logger = LoggerFactory.getLogger(StoreSettingsController.class);

    private final StoreSettingsRepository storeSettingsRepository;

    public StoreSettingsController(StoreSettingsRepository storeSettingsRepository) {
        this.storeSettingsRepository = storeSettingsRepository;
    }

    private StoreSettings getSettings() {
        List<StoreSettings> settingsList = storeSettingsRepository.findAll();
        if (settingsList.isEmpty()) {
            return storeSettingsRepository.save(new StoreSettings());
        }
        return settingsList.get(0);
    }

    @GetMapping
    @Operation(summary = "Get store settings")
    public ResponseEntity<StoreSettings> getStoreSettings() {
        return ResponseEntity.ok(getSettings());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update store settings")
    public ResponseEntity<StoreSettings> updateStoreSettings(@RequestBody StoreSettings updatedSettings) {
        logger.info("Admin updated store settings");
        StoreSettings currentSettings = getSettings();
        currentSettings.setIsAcceptingOrders(updatedSettings.getIsAcceptingOrders());
        storeSettingsRepository.save(currentSettings);
        return ResponseEntity.ok(currentSettings);
    }
}
