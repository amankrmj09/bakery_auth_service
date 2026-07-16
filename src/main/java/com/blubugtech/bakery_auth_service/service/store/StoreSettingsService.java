package com.blubugtech.bakery_auth_service.service.store;

import com.blubugtech.bakery_auth_service.dto.store.StoreSettings;

public interface StoreSettingsService {
    StoreSettings getStoreSettings();
    StoreSettings updateStoreSettings(StoreSettings updatedSettings);
}
