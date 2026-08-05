package com.blubugtech.bakery_auth_service.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAddressRequest {

    private String title;

    @NotBlank(message = "Address line is required")
    private String addressLine;

    private String city;

    private String state;

    @com.fasterxml.jackson.annotation.JsonAlias("zipCode")
    private String postalCode;

    private String country;

    private Boolean isDefault;

    public String getZipCode() {
        return postalCode;
    }

    public void setZipCode(String zipCode) {
        this.postalCode = zipCode;
    }
}
