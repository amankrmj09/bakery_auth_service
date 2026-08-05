package com.blubugtech.bakery_auth_service.mapper;

import com.blubugtech.bakery_auth_service.dto.user.UserResponse;
import com.blubugtech.bakery_auth_service.dto.user.UserAddressResponse;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.entity.UserAddress;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);

    UserAddressResponse toAddressResponse(UserAddress address);

    User toEntity(UserResponse dto);
}
