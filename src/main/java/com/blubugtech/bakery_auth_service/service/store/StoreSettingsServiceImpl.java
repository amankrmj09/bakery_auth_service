package com.blubugtech.bakery_auth_service.service.store;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_auth_service.dto.store.StoreSettings;
import com.blubugtech.bakery_auth_service.mapper.StoreSettingsMapper;
import com.blubugtech.bakery_auth_service.repository.StoreSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class StoreSettingsServiceImpl implements StoreSettingsService {

    private final StoreSettingsRepository storeSettingsRepository;
    private final StoreSettingsMapper storeSettingsMapper;

    public StoreSettingsServiceImpl(StoreSettingsRepository storeSettingsRepository, StoreSettingsMapper storeSettingsMapper) {
        this.storeSettingsRepository = storeSettingsRepository;
        this.storeSettingsMapper = storeSettingsMapper;
    }

    private com.blubugtech.bakery_auth_service.entity.StoreSettings getSettings() {
        List<com.blubugtech.bakery_auth_service.entity.StoreSettings> settingsList = storeSettingsRepository.findAll();
        if (settingsList.isEmpty()) {
            return storeSettingsRepository.save(new com.blubugtech.bakery_auth_service.entity.StoreSettings());
        }
        return settingsList.get(0);
    }

    @Override
    public StoreSettings getStoreSettings() {
        return storeSettingsMapper.toDto(getSettings());
    }

    @Override
    public StoreSettings updateStoreSettings(StoreSettings updatedSettings) {
        log.info("Updating store settings");
        com.blubugtech.bakery_auth_service.entity.StoreSettings currentSettings = getSettings();
        currentSettings.setIsAcceptingOrders(updatedSettings.getIsAcceptingOrders());
        com.blubugtech.bakery_auth_service.entity.StoreSettings savedSettings = storeSettingsRepository.save(currentSettings);
        return storeSettingsMapper.toDto(savedSettings);
    }
}
