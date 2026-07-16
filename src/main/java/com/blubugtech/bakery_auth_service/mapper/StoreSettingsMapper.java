package com.blubugtech.bakery_auth_service.mapper;

import com.blubugtech.bakery_auth_service.dto.store.StoreSettings;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface StoreSettingsMapper {

    StoreSettingsMapper INSTANCE = Mappers.getMapper(StoreSettingsMapper.class);

    StoreSettings toDto(com.blubugtech.bakery_auth_service.entity.StoreSettings entity);

    com.blubugtech.bakery_auth_service.entity.StoreSettings toEntity(StoreSettings dto);
}
