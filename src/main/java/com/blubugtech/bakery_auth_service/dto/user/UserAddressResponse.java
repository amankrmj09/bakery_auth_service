package com.blubugtech.bakery_auth_service.dto.user;

import com.blubugtech.bakery_auth_service.entity.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressResponse {

    private UUID id;
    private String title;
    private String addressLine;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    @Builder.Default
    private Boolean isDefault = false;
}
