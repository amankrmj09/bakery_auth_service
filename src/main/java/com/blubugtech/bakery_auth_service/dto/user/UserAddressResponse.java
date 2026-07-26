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
    private String state;
    @com.fasterxml.jackson.annotation.JsonProperty("postalCode")
    private String postalCode;
    private String country;
    private Boolean isDefault;

    @com.fasterxml.jackson.annotation.JsonProperty("zipCode")
    public String getZipCode() {
        return postalCode;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("zipCode")
    public void setZipCode(String zipCode) {
        this.postalCode = zipCode;
    }

    public static UserAddressResponse from(UserAddress entity) {
        UserAddressResponse dto = new UserAddressResponse();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setAddressLine(entity.getAddressLine());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setPostalCode(entity.getPostalCode());
        dto.setCountry(entity.getCountry());
        dto.setIsDefault(entity.getIsDefault());
        return dto;
    }
}
