package com.blubugtech.bakery_auth_service.mapper;

import com.blubugtech.bakery_auth_service.dto.store.StoreSettings;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface StoreSettingsMapper {

    StoreSettings toDto(com.blubugtech.bakery_auth_service.entity.StoreSettings entity);

    com.blubugtech.bakery_auth_service.entity.StoreSettings toEntity(StoreSettings dto);
}
