package com.shah_s.bakery_auth_service.repository;

import com.shah_s.bakery_auth_service.entity.DashboardStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DashboardStatisticsRepository extends JpaRepository<DashboardStatistics, UUID> {
}
