package com.blubugtech.bakery_auth_service.service.user;

import com.blubugtech.bakery_auth_service.dto.user.UserAddressRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserAddressResponse;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.entity.UserAddress;
import com.blubugtech.bakery_auth_service.exception.AuthException;
import com.blubugtech.bakery_auth_service.repository.UserAddressRepository;
import com.blubugtech.bakery_auth_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserAddressServiceImpl implements UserAddressService {

    private static final int MAX_ADDRESSES = 10;
    
    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;

    public UserAddressServiceImpl(UserAddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<UserAddressResponse> getUserAddresses(UUID userId) {
        return addressRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(UserAddressResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public UserAddressResponse addAddress(UUID userId, UserAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        int currentCount = addressRepository.countByUserId(userId);
        if (currentCount >= MAX_ADDRESSES) {
            throw new AuthException("Maximum limit of " + MAX_ADDRESSES + " addresses reached");
        }

        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setTitle(request.getTitle());
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setZipCode(request.getZipCode());
        
        // If it's the first address or explicitly set as default, make it default
        if (currentCount == 0 || Boolean.TRUE.equals(request.getIsDefault())) {
            setAllAddressesToNonDefault(userId);
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        return UserAddressResponse.from(addressRepository.save(address));
    }

    @Override
    public UserAddressResponse updateAddress(UUID userId, UUID addressId, UserAddressRequest request) {
        UserAddress address = getAddressIfBelongsToUser(userId, addressId);

        address.setTitle(request.getTitle());
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setZipCode(request.getZipCode());

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            setAllAddressesToNonDefault(userId);
            address.setIsDefault(true);
        }

        return UserAddressResponse.from(addressRepository.save(address));
    }

    @Override
    public void deleteAddress(UUID userId, UUID addressId) {
        UserAddress address = getAddressIfBelongsToUser(userId, addressId);
        
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);

        // If default was deleted, set another one as default
        if (wasDefault) {
            List<UserAddress> remaining = addressRepository.findByUserIdOrderByCreatedAtDesc(userId);
            if (!remaining.isEmpty()) {
                UserAddress newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    public void setDefaultAddress(UUID userId, UUID addressId) {
        UserAddress address = getAddressIfBelongsToUser(userId, addressId);
        if (!Boolean.TRUE.equals(address.getIsDefault())) {
            setAllAddressesToNonDefault(userId);
            address.setIsDefault(true);
            addressRepository.save(address);
        }
    }

    private UserAddress getAddressIfBelongsToUser(UUID userId, UUID addressId) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AuthException("Address not found"));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new AuthException("Address does not belong to user");
        }
        return address;
    }

    private void setAllAddressesToNonDefault(UUID userId) {
        List<UserAddress> addresses = addressRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (UserAddress addr : addresses) {
            if (Boolean.TRUE.equals(addr.getIsDefault())) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }
    }
}
