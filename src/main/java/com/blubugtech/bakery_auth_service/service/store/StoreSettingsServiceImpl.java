package com.blubugtech.bakery_auth_service.service.store;

import com.blubugtech.bakery_auth_service.dto.store.StoreSettings;
import com.blubugtech.bakery_auth_service.mapper.StoreSettingsMapper;
import com.blubugtech.bakery_auth_service.repository.StoreSettingsRepository;
import org.blubakery.common.messaging.contract.messaging.SettingsPayload;
import org.blubakery.common.messaging.event.SettingsEvent;
import org.blubakery.common.messaging.constants.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class StoreSettingsServiceImpl implements StoreSettingsService {

    private final StoreSettingsRepository storeSettingsRepository;
    private final StoreSettingsMapper storeSettingsMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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
        currentSettings.setAdminNotificationEmail(updatedSettings.getAdminNotificationEmail());
        
        com.blubugtech.bakery_auth_service.entity.StoreSettings savedSettings = storeSettingsRepository.save(currentSettings);
        
        // Publish SettingsEvent
        SettingsPayload payload = SettingsPayload.builder()
                .adminNotificationEmail(savedSettings.getAdminNotificationEmail())
                .build();
        SettingsEvent event = new SettingsEvent(payload);
        
        try {
            kafkaTemplate.send(KafkaTopics.SETTINGS_TOPIC, event);
            log.info("Published SettingsEvent for updated settings");
        } catch (Exception e) {
            log.error("Failed to publish SettingsEvent", e);
        }
        
        return storeSettingsMapper.toDto(savedSettings);
    }
}
