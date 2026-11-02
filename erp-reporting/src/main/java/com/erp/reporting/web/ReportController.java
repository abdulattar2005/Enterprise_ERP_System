package com.erp.reporting.web;

import com.erp.accounting.dto.TrialBalanceReport;
import com.erp.accounting.service.TrialBalanceService;
import com.erp.inventory.dto.ProductDto;
import com.erp.inventory.service.ProductService;
import com.erp.reporting.dto.DashboardDto;
import com.erp.reporting.dto.SalesSummaryDto;
import com.erp.reporting.service.ReportService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ReportController {

    private final ReportService reports;
    private final ProductService productService;
    private final TrialBalanceService trialBalance;

    public ReportController(ReportService reports, ProductService productService, TrialBalanceService trialBalance) {
        this.reports = reports;
        this.productService = productService;
        this.trialBalance = trialBalance;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard() {
        return reports.dashboard();
    }

    @GetMapping("/sales-summary")
    public SalesSummaryDto salesSummary() {
        return reports.salesSummary();
    }

    @GetMapping("/low-stock")
    public List<ProductDto> lowStock() {
        return productService.lowStock();
    }

    @GetMapping("/trial-balance")
    public TrialBalanceReport trialBalance() {
        return trialBalance.report();
    }
}
