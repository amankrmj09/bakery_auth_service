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
    @com.fasterxml.jackson.annotation.JsonProperty("postalCode")
    private String postalCode;
    private String country;
    @Builder.Default
    private Boolean isDefault = false;

    @com.fasterxml.jackson.annotation.JsonProperty("zipCode")
    public String getZipCode() {
        return postalCode;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("zipCode")
    public void setZipCode(String zipCode) {
        this.postalCode = zipCode;
    }
}
