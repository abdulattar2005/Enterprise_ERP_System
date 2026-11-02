package com.erp.reporting.service;

import com.erp.accounting.service.TrialBalanceService;
import com.erp.hr.domain.EmployeeStatus;
import com.erp.hr.repository.EmployeeRepository;
import com.erp.inventory.repository.ProductRepository;
import com.erp.reporting.dto.DashboardDto;
import com.erp.reporting.dto.SalesSummaryDto;
import com.erp.sales.domain.OrderStatus;
import com.erp.sales.repository.SalesOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-only cross-module reporting. Never writes. */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final EmployeeRepository employees;
    private final ProductRepository products;
    private final SalesOrderRepository salesOrders;
    private final TrialBalanceService trialBalance;

    public ReportService(EmployeeRepository employees, ProductRepository products,
                         SalesOrderRepository salesOrders, TrialBalanceService trialBalance) {
        this.employees = employees;
        this.products = products;
        this.salesOrders = salesOrders;
        this.trialBalance = trialBalance;
    }

    public DashboardDto dashboard() {
        return new DashboardDto(
                employees.countByStatus(EmployeeStatus.ACTIVE),
                products.countByActiveTrue(),
                products.findLowStock().size(),
                products.inventoryValuation(),
                salesOrders.countByStatus(OrderStatus.DRAFT),
                salesOrders.countByStatus(OrderStatus.CONFIRMED),
                salesOrders.sumTotalByStatus(OrderStatus.CONFIRMED),
                trialBalance.report().balanced());
    }

    public SalesSummaryDto salesSummary() {
        long confirmed = salesOrders.countByStatus(OrderStatus.CONFIRMED);
        BigDecimal revenue = salesOrders.sumTotalByStatus(OrderStatus.CONFIRMED);
        BigDecimal average = confirmed == 0
                ? BigDecimal.ZERO.setScale(2)
                : revenue.divide(BigDecimal.valueOf(confirmed), 2, RoundingMode.HALF_UP);
        return new SalesSummaryDto(salesOrders.countByStatus(OrderStatus.DRAFT), confirmed,
                salesOrders.countByStatus(OrderStatus.CANCELLED), revenue, average);
    }
}
