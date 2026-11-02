package com.erp.reporting.dto;

import java.math.BigDecimal;

public record SalesSummaryDto(long draft, long confirmed, long cancelled, BigDecimal confirmedRevenue,
                              BigDecimal averageConfirmedOrderValue) {
}
