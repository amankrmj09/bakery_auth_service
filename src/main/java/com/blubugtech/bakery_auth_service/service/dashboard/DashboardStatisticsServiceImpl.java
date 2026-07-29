package com.blubugtech.bakery_auth_service.service.dashboard;

import com.blubugtech.bakery_auth_service.entity.DashboardStatistics;
import com.blubugtech.bakery_auth_service.entity.DashboardStatisticsSnapshot;
import com.blubugtech.bakery_auth_service.repository.DashboardStatisticsRepository;
import com.blubugtech.bakery_auth_service.repository.DashboardStatisticsSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class DashboardStatisticsServiceImpl implements DashboardStatisticsService {

    private final DashboardStatisticsRepository dashboardStatisticsRepository;
    private final DashboardStatisticsSnapshotRepository snapshotRepository;

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
        LocalDate previousPeriodDate;
        if ("1d".equalsIgnoreCase(timeframe)) {
            pastDate = today.minusDays(1);
            previousPeriodDate = today.minusDays(2);
        } else if ("7d".equalsIgnoreCase(timeframe)) {
            pastDate = today.minusDays(7);
            previousPeriodDate = today.minusDays(14);
        } else {
            pastDate = today.minusMonths(1);
            previousPeriodDate = today.minusMonths(2);
        }

        Optional<DashboardStatisticsSnapshot> pastSnapshotOpt = snapshotRepository.findFirstBySnapshotDateLessThanEqualOrderBySnapshotDateDesc(pastDate);
        Optional<DashboardStatisticsSnapshot> previousSnapshotOpt = snapshotRepository.findFirstBySnapshotDateLessThanEqualOrderBySnapshotDateDesc(previousPeriodDate);

        BigDecimal totalAtPastDate = pastSnapshotOpt.map(DashboardStatisticsSnapshot::getTotalRevenue).orElse(BigDecimal.ZERO);
        BigDecimal totalAtPreviousDate = previousSnapshotOpt.map(DashboardStatisticsSnapshot::getTotalRevenue).orElse(BigDecimal.ZERO);

        BigDecimal currentTotal = currentStats.getTotalRevenue();

        BigDecimal currentPeriodRevenue = currentTotal.subtract(totalAtPastDate);
        if (currentPeriodRevenue.compareTo(BigDecimal.ZERO) < 0) {
            currentPeriodRevenue = BigDecimal.ZERO;
        }

        BigDecimal previousPeriodRevenue = totalAtPastDate.subtract(totalAtPreviousDate);
        if (previousPeriodRevenue.compareTo(BigDecimal.ZERO) < 0) {
            previousPeriodRevenue = BigDecimal.ZERO;
        }

        double growthRate = 0.0;
        if (previousPeriodRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = currentPeriodRevenue.subtract(previousPeriodRevenue)
                    .divide(previousPeriodRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        } else if (currentPeriodRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = 100.0;
        }

        List<DashboardStatisticsSnapshot> snapshots = snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(pastDate, today);
        List<Map<String, Object>> chartData = new java.util.ArrayList<>();

        BigDecimal previousCumulative = BigDecimal.ZERO;
        if (!snapshots.isEmpty()) {
            Optional<DashboardStatisticsSnapshot> beforeStart = snapshotRepository.findFirstBySnapshotDateLessThanEqualOrderBySnapshotDateDesc(snapshots.get(0).getSnapshotDate().minusDays(1));
            if (beforeStart.isPresent()) {
                previousCumulative = beforeStart.get().getTotalRevenue();
            }
        }

        for (DashboardStatisticsSnapshot snap : snapshots) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("name", snap.getSnapshotDate().toString()); // Simple ISO date string

            BigDecimal currentCumulative = snap.getTotalRevenue();
            BigDecimal dailyRevenue = currentCumulative.subtract(previousCumulative);
            if (dailyRevenue.compareTo(BigDecimal.ZERO) < 0) {
                dailyRevenue = BigDecimal.ZERO;
            }
            dataPoint.put("revenue", dailyRevenue);

            chartData.add(dataPoint);
            previousCumulative = currentCumulative;
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
        log.info("Incremented total users to {}", stats.getTotalUsers());
    }

    @Transactional
    public void decrementUsers() {
        DashboardStatistics stats = getStatistics();
        long newTotal = Math.max(0, stats.getTotalUsers() - 1);
        stats.setTotalUsers(newTotal);
        dashboardStatisticsRepository.save(stats);
        updateSnapshot(stats);
        log.info("Decremented total users to {}", stats.getTotalUsers());
    }

    @Transactional
    public void incrementOrders() {
        DashboardStatistics stats = getStatistics();
        stats.setActiveOrders(stats.getActiveOrders() + 1);
        dashboardStatisticsRepository.save(stats);
        updateSnapshot(stats);
        log.info("Incremented active orders to {}", stats.getActiveOrders());
    }

    @Transactional
    public void decrementOrders() {
        DashboardStatistics stats = getStatistics();
        long newActive = Math.max(0, stats.getActiveOrders() - 1);
        stats.setActiveOrders(newActive);
        dashboardStatisticsRepository.save(stats);
        updateSnapshot(stats);
        log.info("Decremented active orders to {}", stats.getActiveOrders());
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
        log.info("Added {} to total revenue, new total: {}", amount, stats.getTotalRevenue());
    }
}
