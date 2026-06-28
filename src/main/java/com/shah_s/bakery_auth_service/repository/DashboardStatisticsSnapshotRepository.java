package com.shah_s.bakery_auth_service.repository;

import com.shah_s.bakery_auth_service.entity.DashboardStatisticsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DashboardStatisticsSnapshotRepository extends JpaRepository<DashboardStatisticsSnapshot, UUID> {
    Optional<DashboardStatisticsSnapshot> findBySnapshotDate(LocalDate snapshotDate);
    List<DashboardStatisticsSnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate startDate, LocalDate endDate);
}
