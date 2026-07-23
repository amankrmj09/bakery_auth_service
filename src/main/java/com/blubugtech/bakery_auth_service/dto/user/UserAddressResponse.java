package com.blubugtech.bakery_auth_service.dto.user;

import com.blubugtech.bakery_auth_service.entity.UserAddress;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class UserAddressResponse {
    
    private UUID id;
    private String title;
    private String addressLine;
    private String city;
    private String zipCode;
    private Boolean isDefault;

    public static UserAddressResponse from(UserAddress entity) {
        UserAddressResponse dto = new UserAddressResponse();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setAddressLine(entity.getAddressLine());
        dto.setCity(entity.getCity());
        dto.setZipCode(entity.getZipCode());
        dto.setIsDefault(entity.getIsDefault());
        return dto;
    }
}
