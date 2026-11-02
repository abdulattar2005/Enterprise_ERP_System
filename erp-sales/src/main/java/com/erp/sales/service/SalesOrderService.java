package com.erp.sales.service;

import com.erp.core.audit.AuditService;
import com.erp.core.event.SalesInvoicedEvent;
import com.erp.core.exception.BusinessRuleException;
import com.erp.core.exception.ResourceNotFoundException;
import com.erp.inventory.dto.ProductDto;
import com.erp.inventory.service.ProductService;
import com.erp.sales.domain.Customer;
import com.erp.sales.domain.OrderStatus;
import com.erp.sales.domain.SalesOrder;
import com.erp.sales.domain.SalesOrderLine;
import com.erp.sales.dto.SalesOrderDto;
import com.erp.sales.dto.SalesOrderRequest;
import com.erp.sales.repository.CustomerRepository;
import com.erp.sales.repository.SalesOrderRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SalesOrderService {

    private final SalesOrderRepository orders;
    private final CustomerRepository customers;
    private final ProductService productService;
    private final AuditService audit;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public SalesOrderService(SalesOrderRepository orders, CustomerRepository customers,
                             ProductService productService, AuditService audit, ApplicationEventPublisher events,
                             Clock clock) {
        this.orders = orders;
        this.customers = customers;
        this.productService = productService;
        this.audit = audit;
        this.events = events;
        this.clock = clock;
    }

    /** Creates a DRAFT order priced from the current product prices. Stock is not touched yet. */
    public SalesOrderDto create(SalesOrderRequest request) {
        Customer customer = customers.findById(request.customerId())
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", request.customerId()));
        SalesOrder order = new SalesOrder();
        order.setCustomer(customer);
        order.setOrderDate(LocalDate.now(clock));
        for (SalesOrderRequest.Line l : request.lines()) {
            ProductDto product = productService.get(l.productId());
            if (!product.active()) {
                throw new BusinessRuleException("Product " + product.sku() + " is not active");
            }
            SalesOrderLine line = new SalesOrderLine();
            line.setProductId(product.id());
            line.setProductName(product.name());
            line.setQuantity(l.quantity());
            line.setUnitPrice(product.unitPrice());
            line.setLineTotal(product.unitPrice().multiply(BigDecimal.valueOf(l.quantity())));
            order.addLine(line);
        }
        SalesOrder saved = orders.save(order);
        audit.log("CREATE_SALES_ORDER", "Sales order id=" + saved.getId() + " for " + customer.getName());
        return SalesOrderDto.from(saved);
    }

    /**
     * Confirming reserves stock and raises the invoice. All steps run in one transaction: if any line has
     * insufficient stock, the whole confirmation (including earlier stock deductions) is rolled back.
     */
    public SalesOrderDto confirm(Long id) {
        SalesOrder order = find(id);
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessRuleException("Only DRAFT orders can be confirmed (current: " + order.getStatus() + ")");
        }
        for (SalesOrderLine line : order.getLines()) {
            productService.adjustStock(line.getProductId(), -line.getQuantity(), "Sales order #" + id);
        }
        order.setStatus(OrderStatus.CONFIRMED);
        events.publishEvent(new SalesInvoicedEvent(order.getId(), order.getCustomer().getName(),
                order.getTotalAmount()));
        audit.log("CONFIRM_SALES_ORDER", "Confirmed sales order id=" + id + ", total=" + order.getTotalAmount());
        return SalesOrderDto.from(order);
    }

    /** Only DRAFT orders can be cancelled; a confirmed order has already moved stock and the ledger. */
    public SalesOrderDto cancel(Long id) {
        SalesOrder order = find(id);
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessRuleException("Only DRAFT orders can be cancelled (current: " + order.getStatus() + ")");
        }
        order.setStatus(OrderStatus.CANCELLED);
        audit.log("CANCEL_SALES_ORDER", "Cancelled sales order id=" + id);
        return SalesOrderDto.from(order);
    }

    @Transactional(readOnly = true)
    public SalesOrderDto get(Long id) {
        return SalesOrderDto.from(find(id));
    }

    @Transactional(readOnly = true)
    public Page<SalesOrderDto> list(Pageable pageable) {
        return orders.findAll(pageable).map(SalesOrderDto::from);
    }

    private SalesOrder find(Long id) {
        return orders.findById(id).orElseThrow(() -> ResourceNotFoundException.of("SalesOrder", id));
    }
}
