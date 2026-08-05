package com.blubugtech.bakery_auth_service.service.user;

import com.blubugtech.bakery_auth_service.dto.user.UserAddressRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserAddressResponse;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

public interface UserAddressService {
    PagedModel<UserAddressResponse> getUserAddresses(UUID userId, Pageable pageable);

    UserAddressResponse addAddress(UUID userId, UserAddressRequest request);

    UserAddressResponse updateAddress(UUID userId, UUID addressId, UserAddressRequest request);

    void deleteAddress(UUID userId, UUID addressId);

    void setDefaultAddress(UUID userId, UUID addressId);
}
