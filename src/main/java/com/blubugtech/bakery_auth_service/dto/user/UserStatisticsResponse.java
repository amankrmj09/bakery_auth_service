package com.blubugtech.bakery_auth_service.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {
    private Long totalUsers;
    private Long TOTAL_USERS;
    private Long activeUsers;
    private Long verifiedUsers;
    private Long adminUsers;
}
