package com.blubugtech.bakery_auth_service.service.dashboard;

import com.blubugtech.bakery_auth_service.entity.DashboardStatistics;
import java.math.BigDecimal;
import java.util.Map;

public interface DashboardStatisticsService {
    DashboardStatistics getStatistics();
    Map<String, Object> getStatisticsWithGrowth(String timeframe);
    void incrementUsers();
    void decrementUsers();
    void incrementOrders();
    void decrementOrders();
    void addRevenue(BigDecimal amount);
}
