package com.blubugtech.bakery_auth_service.service;

import com.blubugtech.bakery_auth_service.entity.DashboardStatistics;
import com.blubugtech.bakery_auth_service.entity.DashboardStatisticsSnapshot;
import com.blubugtech.bakery_auth_service.repository.DashboardStatisticsRepository;
import com.blubugtech.bakery_auth_service.repository.DashboardStatisticsSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DashboardStatisticsService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardStatisticsService.class);

    private final DashboardStatisticsRepository dashboardStatisticsRepository;
    private final DashboardStatisticsSnapshotRepository snapshotRepository;

    public DashboardStatisticsService(DashboardStatisticsRepository dashboardStatisticsRepository,
                                      DashboardStatisticsSnapshotRepository snapshotRepository) {
        this.dashboardStatisticsRepository = dashboardStatisticsRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @Transactional
    public DashboardStatistics getStatistics() {
        List<DashboardStatistics> stats = dashboardStatisticsRepository.findAll();
        if (stats.isEmpty()) {
            return dashboardStatisticsRepository.save(new DashboardStatistics());
        }
        return stats.get(0);
    }

    @Transactional
    public Map<String, Object> getStatisticsWithGrowth(String timeframe) {
        DashboardStatistics currentStats = getStatistics();
        LocalDate today = LocalDate.now();
        LocalDate pastDate;
        if ("7d".equalsIgnoreCase(timeframe)) {
            pastDate = today.minusDays(7);
        } else {
            pastDate = today.minusMonths(1);
        }

        Optional<DashboardStatisticsSnapshot> pastSnapshotOpt = snapshotRepository.findBySnapshotDate(pastDate);
        BigDecimal pastRevenue = BigDecimal.ZERO;
        
        if (pastSnapshotOpt.isPresent()) {
            pastRevenue = pastSnapshotOpt.get().getTotalRevenue();
        }

        BigDecimal currentRevenue = currentStats.getTotalRevenue();
        double growthRate = 0.0;
        if (pastRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = currentRevenue.subtract(pastRevenue)
                    .divide(pastRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        } else if (currentRevenue.compareTo(BigDecimal.ZERO) > 0 && pastSnapshotOpt.isPresent()) {
            growthRate = 100.0;
        }

        List<DashboardStatisticsSnapshot> snapshots = snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(pastDate, today);
        List<Map<String, Object>> chartData = new java.util.ArrayList<>();
        for (DashboardStatisticsSnapshot snap : snapshots) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("name", snap.getSnapshotDate().toString()); // Simple ISO date string
            dataPoint.put("revenue", snap.getTotalRevenue());
            chartData.add(dataPoint);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", currentStats.getTotalUsers());
        response.put("activeOrders", currentStats.getActiveOrders());
        response.put("totalRevenue", currentStats.getTotalRevenue());
        response.put("growthRate", growthRate);
        response.put("timeframe", timeframe);
        response.put("chartData", chartData);
        return response;
    }

    private void updateSnapshot(DashboardStatistics stats) {
        LocalDate today = LocalDate.now();
        DashboardStatisticsSnapshot snapshot = snapshotRepository.findBySnapshotDate(today)
                .orElse(DashboardStatisticsSnapshot.builder().snapshotDate(today).build());
        
        snapshot.setTotalUsers(stats.getTotalUsers());
        snapshot.setActiveOrders(stats.getActiveOrders());
        snapshot.setTotalRevenue(stats.getTotalRevenue());
        snapshotRepository.save(snapshot);
    }

    @Transactional
    public void incrementUsers() {
        DashboardStatistics stats = getStatistics();
        stats.setTotalUsers(stats.getTotalUsers() + 1);
        dashboardStatisticsRepository.save(stats);
        updateSnapshot(stats);
        logger.info("Incremented total users to {}", stats.getTotalUsers());
    }

    @Transactional
    public void decrementUsers() {
        DashboardStatistics stats = getStatistics();
        long newTotal = Math.max(0, stats.getTotalUsers() - 1);
        stats.setTotalUsers(newTotal);
        dashboardStatisticsRepository.save(stats);
        updateSnapshot(stats);
        logger.info("Decremented total users to {}", stats.getTotalUsers());
    }

    @Transactional
    public void incrementOrders() {
        DashboardStatistics stats = getStatistics();
        stats.setActiveOrders(stats.getActiveOrders() + 1);
        dashboardStatisticsRepository.save(stats);
        updateSnapshot(stats);
        logger.info("Incremented active orders to {}", stats.getActiveOrders());
    }

    @Transactional
    public void decrementOrders() {
        DashboardStatistics stats = getStatistics();
        long newActive = Math.max(0, stats.getActiveOrders() - 1);
        stats.setActiveOrders(newActive);
        dashboardStatisticsRepository.save(stats);
        updateSnapshot(stats);
        logger.info("Decremented active orders to {}", stats.getActiveOrders());
    }

    @Transactional
    public void addRevenue(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        DashboardStatistics stats = getStatistics();
        stats.setTotalRevenue(stats.getTotalRevenue().add(amount));
        dashboardStatisticsRepository.save(stats);
        updateSnapshot(stats);
        logger.info("Added {} to total revenue, new total: {}", amount, stats.getTotalRevenue());
    }
}
