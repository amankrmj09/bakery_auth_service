package com.blubugtech.bakery_auth_service.repository;

import com.blubugtech.bakery_auth_service.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
    Page<UserAddress> findByUserId(UUID userId, Pageable pageable);

    List<UserAddress> findByUserIdOrderByCreatedAtDesc(UUID userId);

    int countByUserId(UUID userId);
}
