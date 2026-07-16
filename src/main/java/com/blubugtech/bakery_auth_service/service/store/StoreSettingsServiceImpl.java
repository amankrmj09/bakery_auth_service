package com.blubugtech.bakery_auth_service.service.store;

import com.blubugtech.bakery_auth_service.dto.store.StoreSettings;
import com.blubugtech.bakery_auth_service.mapper.StoreSettingsMapper;
import com.blubugtech.bakery_auth_service.repository.StoreSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StoreSettingsServiceImpl implements StoreSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(StoreSettingsService.class);

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
        logger.info("Updating store settings");
        com.blubugtech.bakery_auth_service.entity.StoreSettings currentSettings = getSettings();
        currentSettings.setIsAcceptingOrders(updatedSettings.getIsAcceptingOrders());
        com.blubugtech.bakery_auth_service.entity.StoreSettings savedSettings = storeSettingsRepository.save(currentSettings);
        return storeSettingsMapper.toDto(savedSettings);
    }
}
