package com.blubugtech.bakery_auth_service.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private BigDecimal currentPeriodRevenue;
    private BigDecimal previousPeriodRevenue;
    private BigDecimal growthPercentage;
    private Long totalUsers;
    private Long activeOrders;
    private BigDecimal totalRevenue;
    private String timeframe;
    private java.util.List<ChartDataResponse> chartData;
}
