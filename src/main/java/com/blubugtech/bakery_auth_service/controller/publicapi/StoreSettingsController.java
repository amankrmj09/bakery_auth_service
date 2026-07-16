package com.blubugtech.bakery_auth_service.controller.publicapi;

import com.blubugtech.bakery_auth_service.dto.store.StoreSettings;
import com.blubugtech.bakery_auth_service.service.store.StoreSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping({"/api/store/settings", "/api/store/settings/"})
@Tag(name = "Store Settings", description = "Endpoints for managing global bakery settings")
public class StoreSettingsController {
    private static final Logger logger = LoggerFactory.getLogger(StoreSettingsController.class);

    private final StoreSettingsService storeSettingsService;

    public StoreSettingsController(StoreSettingsService storeSettingsService) {
        this.storeSettingsService = storeSettingsService;
    }

    @GetMapping
    @Operation(summary = "Get store settings")
    public ResponseEntity<StoreSettings> getStoreSettings() {
        return ResponseEntity.ok(storeSettingsService.getStoreSettings());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update store settings")
    public ResponseEntity<StoreSettings> updateStoreSettings(@RequestBody StoreSettings updatedSettings) {
        logger.info("Admin updated store settings");
        StoreSettings result = storeSettingsService.updateStoreSettings(updatedSettings);
        return ResponseEntity.ok(result);
    }
}
