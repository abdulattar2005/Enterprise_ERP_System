package com.erp.reporting.dto;

import java.math.BigDecimal;

public record DashboardDto(long activeEmployees, long activeProducts, long lowStockProducts,
                           BigDecimal inventoryValuation, long draftOrders, long confirmedOrders,
                           BigDecimal confirmedRevenue, boolean ledgerBalanced) {
}
