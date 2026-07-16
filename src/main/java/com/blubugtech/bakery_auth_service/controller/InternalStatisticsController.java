package com.blubugtech.bakery_auth_service.controller;

import com.blubugtech.bakery_auth_service.service.DashboardStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/users/internal/stats")
@Tag(name = "Internal Statistics", description = "Endpoints for internal metrics and statistics")
public class InternalStatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(InternalStatisticsController.class);
    private final DashboardStatisticsService statisticsService;

    public InternalStatisticsController(DashboardStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    private boolean isSystemAuthorized(String userRole) {
        return "SYSTEM".equals(userRole);
    }

    @PostMapping("/increment-orders")
    @Operation(summary = "Increment total orders")
    public ResponseEntity<Void> incrementOrders(@RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!isSystemAuthorized(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        statisticsService.incrementOrders();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/decrement-orders")
    @Operation(summary = "Decrement total orders")
    public ResponseEntity<Void> decrementOrders(@RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!isSystemAuthorized(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        statisticsService.decrementOrders();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add-revenue")
    @Operation(summary = "Add revenue to statistics")
    public ResponseEntity<Void> addRevenue(@RequestBody Map<String, Object> payload, @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!isSystemAuthorized(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Object amountObj = payload.get("amount");
        BigDecimal amount;
        if (amountObj instanceof Number) {
            amount = new BigDecimal(amountObj.toString());
        } else if (amountObj instanceof String) {
            amount = new BigDecimal((String) amountObj);
        } else {
            throw new IllegalArgumentException("Invalid amount format");
        }
        statisticsService.addRevenue(amount);
        return ResponseEntity.ok().build();
    }
}
