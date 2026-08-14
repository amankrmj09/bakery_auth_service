package com.blubugtech.bakery_auth_service.unit;

import com.blubugtech.bakery_auth_service.dto.user.UserAddressRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserAddressResponse;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.entity.UserAddress;
import com.blubugtech.bakery_auth_service.exception.AuthException;
import com.blubugtech.bakery_auth_service.mapper.UserMapper;
import com.blubugtech.bakery_auth_service.repository.UserAddressRepository;
import com.blubugtech.bakery_auth_service.repository.UserRepository;
import com.blubugtech.bakery_auth_service.service.user.UserAddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.blubakery.common.core.dto.RestPageResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAddressServiceImplTest {

    @Mock
    private UserAddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserAddressServiceImpl addressService;

    private User testUser;
    private UUID testUserId;
    private UserAddress testAddress;
    private UUID testAddressId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(testUserId);

        testAddressId = UUID.randomUUID();
        testAddress = new UserAddress();
        testAddress.setId(testAddressId);
        testAddress.setUser(testUser);
        testAddress.setAddressLine("123 Main St");
        testAddress.setIsDefault(true);
    }

    @Test
    void getUserAddresses_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<UserAddress> addressList = List.of(testAddress);
        Page<UserAddress> addressPage = new PageImpl<>(addressList);
        
        when(addressRepository.findByUserId(testUserId, pageable)).thenReturn(addressPage);
        UserAddressResponse response = new UserAddressResponse();
        response.setAddressLine("123 Main St");
        when(userMapper.toAddressResponse(testAddress)).thenReturn(response);

        // Act
        RestPageResponse<UserAddressResponse> result = addressService.getUserAddresses(testUserId, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAddressLine()).isEqualTo("123 Main St");
    }

    @Test
    void addAddress_Success() {
        // Arrange
        UserAddressRequest request = new UserAddressRequest();
        request.setAddressLine("456 Elm St");
        request.setIsDefault(false);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(addressRepository.countByUserId(testUserId)).thenReturn(1);
        when(addressRepository.save(any(UserAddress.class))).thenReturn(testAddress);
        UserAddressResponse response = new UserAddressResponse();
        response.setAddressLine("456 Elm St");
        when(userMapper.toAddressResponse(any())).thenReturn(response);

        // Act
        UserAddressResponse result = addressService.addAddress(testUserId, request);

        // Assert
        assertThat(result.getAddressLine()).isEqualTo("456 Elm St");
        verify(addressRepository, times(1)).save(any(UserAddress.class));
    }

    @Test
    void addAddress_ThrowsIfLimitReached() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(addressRepository.countByUserId(testUserId)).thenReturn(10); // MAX_ADDRESSES

        // Act & Assert
        assertThrows(AuthException.class, () -> addressService.addAddress(testUserId, new UserAddressRequest()));
        verify(addressRepository, never()).save(any());
    }

    @Test
    void updateAddress_Success() {
        // Arrange
        UserAddressRequest request = new UserAddressRequest();
        request.setAddressLine("789 Pine St");
        
        when(addressRepository.findById(testAddressId)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(UserAddress.class))).thenReturn(testAddress);
        UserAddressResponse response = new UserAddressResponse();
        response.setAddressLine("789 Pine St");
        when(userMapper.toAddressResponse(any())).thenReturn(response);

        // Act
        UserAddressResponse result = addressService.updateAddress(testUserId, testAddressId, request);

        // Assert
        assertThat(result.getAddressLine()).isEqualTo("789 Pine St");
        verify(addressRepository, times(1)).save(any(UserAddress.class));
    }

    @Test
    void updateAddress_ThrowsIfNotBelongingToUser() {
        // Arrange
        User wrongUser = new User();
        wrongUser.setId(UUID.randomUUID()); // Different user ID
        testAddress.setUser(wrongUser);
        
        when(addressRepository.findById(testAddressId)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        assertThrows(AuthException.class, () -> addressService.updateAddress(testUserId, testAddressId, new UserAddressRequest()));
        verify(addressRepository, never()).save(any());
    }

    @Test
    void deleteAddress_Success() {
        // Arrange
        when(addressRepository.findById(testAddressId)).thenReturn(Optional.of(testAddress));
        when(addressRepository.findByUserIdOrderByCreatedAtDesc(testUserId)).thenReturn(new ArrayList<>());

        // Act
        addressService.deleteAddress(testUserId, testAddressId);

        // Assert
        verify(addressRepository, times(1)).delete(testAddress);
    }
}
