package com.blubugtech.bakery_auth_service.repository;

import com.blubugtech.bakery_auth_service.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
    List<UserAddress> findByUserIdOrderByCreatedAtDesc(UUID userId);
    int countByUserId(UUID userId);
}
