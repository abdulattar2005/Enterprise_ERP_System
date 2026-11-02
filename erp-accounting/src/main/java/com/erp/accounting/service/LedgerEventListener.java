package com.erp.accounting.service;

import com.erp.accounting.dto.LineSpec;
import com.erp.core.event.PayrollProcessedEvent;
import com.erp.core.event.PurchaseReceivedEvent;
import com.erp.core.event.SalesInvoicedEvent;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Turns business events from other modules into balanced journal entries. Listeners run synchronously in the
 * publisher's transaction, so a business action and its ledger entry commit or roll back together.
 */
@Component
public class LedgerEventListener {

    public static final String CASH = "1000";
    public static final String RECEIVABLES = "1100";
    public static final String INVENTORY = "1200";
    public static final String PAYABLES = "2000";
    public static final String TAXES_PAYABLE = "2100";
    public static final String SALES_REVENUE = "4000";
    public static final String SALARIES_EXPENSE = "5100";

    private final JournalService journal;

    public LedgerEventListener(JournalService journal) {
        this.journal = journal;
    }

    @EventListener
    public void on(SalesInvoicedEvent e) {
        journal.postSystemEntry("Sales invoice for order #" + e.orderId() + " (" + e.customerName() + ")",
                "SO-" + e.orderId(),
                List.of(new LineSpec(RECEIVABLES, e.amount(), BigDecimal.ZERO),
                        new LineSpec(SALES_REVENUE, BigDecimal.ZERO, e.amount())));
    }

    @EventListener
    public void on(PurchaseReceivedEvent e) {
        journal.postSystemEntry("Goods received from " + e.supplierName() + " (PO #" + e.purchaseOrderId() + ")",
                "PO-" + e.purchaseOrderId(),
                List.of(new LineSpec(INVENTORY, e.amount(), BigDecimal.ZERO),
                        new LineSpec(PAYABLES, BigDecimal.ZERO, e.amount())));
    }

    @EventListener
    public void on(PayrollProcessedEvent e) {
        // Dr Salaries (gross) = Cr Cash (net) + Cr Taxes payable (tax); the tax line is omitted when zero.
        List<LineSpec> lines = e.totalTax().signum() > 0
                ? List.of(new LineSpec(SALARIES_EXPENSE, e.totalGross(), BigDecimal.ZERO),
                        new LineSpec(CASH, BigDecimal.ZERO, e.totalNet()),
                        new LineSpec(TAXES_PAYABLE, BigDecimal.ZERO, e.totalTax()))
                : List.of(new LineSpec(SALARIES_EXPENSE, e.totalGross(), BigDecimal.ZERO),
                        new LineSpec(CASH, BigDecimal.ZERO, e.totalNet()));
        journal.postSystemEntry("Payroll for " + e.period(), "PAY-" + e.period(), lines);
    }
}
