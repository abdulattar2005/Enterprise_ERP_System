package com.erp.inventory.service;

import com.erp.core.audit.AuditService;
import com.erp.core.event.PurchaseReceivedEvent;
import com.erp.core.exception.BusinessRuleException;
import com.erp.core.exception.ResourceNotFoundException;
import com.erp.inventory.domain.Product;
import com.erp.inventory.domain.PurchaseOrder;
import com.erp.inventory.domain.PurchaseOrderLine;
import com.erp.inventory.domain.PurchaseOrderStatus;
import com.erp.inventory.domain.Supplier;
import com.erp.inventory.dto.PurchaseOrderDto;
import com.erp.inventory.dto.PurchaseOrderRequest;
import com.erp.inventory.repository.ProductRepository;
import com.erp.inventory.repository.PurchaseOrderRepository;
import com.erp.inventory.repository.SupplierRepository;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository orders;
    private final SupplierRepository suppliers;
    private final ProductRepository products;
    private final ProductService productService;
    private final AuditService audit;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public PurchaseOrderService(PurchaseOrderRepository orders, SupplierRepository suppliers,
                                ProductRepository products, ProductService productService, AuditService audit,
                                ApplicationEventPublisher events, Clock clock) {
        this.orders = orders;
        this.suppliers = suppliers;
        this.products = products;
        this.productService = productService;
        this.audit = audit;
        this.events = events;
        this.clock = clock;
    }

    public PurchaseOrderDto create(PurchaseOrderRequest request) {
        Supplier supplier = suppliers.findById(request.supplierId())
                .orElseThrow(() -> ResourceNotFoundException.of("Supplier", request.supplierId()));
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setOrderDate(LocalDate.now(clock));
        for (PurchaseOrderRequest.Line l : request.lines()) {
            Product product = products.findById(l.productId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Product", l.productId()));
            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setProduct(product);
            line.setQuantity(l.quantity());
            line.setUnitCost(l.unitCost());
            order.addLine(line);
        }
        PurchaseOrder saved = orders.save(order);
        audit.log("CREATE_PURCHASE_ORDER", "PO id=" + saved.getId() + " for supplier " + supplier.getName());
        return PurchaseOrderDto.from(saved);
    }

    /** Receiving goods increases stock for every line and tells Accounting (inventory up, payable up). */
    public PurchaseOrderDto receive(Long id) {
        PurchaseOrder order = orders.findById(id).orElseThrow(() -> ResourceNotFoundException.of("PurchaseOrder", id));
        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BusinessRuleException("Only DRAFT purchase orders can be received");
        }
        for (PurchaseOrderLine line : order.getLines()) {
            productService.adjustStock(line.getProduct().getId(), line.getQuantity(), "Received PO #" + id);
        }
        order.setStatus(PurchaseOrderStatus.RECEIVED);
        events.publishEvent(new PurchaseReceivedEvent(order.getId(), order.getSupplier().getName(),
                order.totalAmount()));
        audit.log("RECEIVE_PURCHASE_ORDER", "Received PO id=" + id);
        return PurchaseOrderDto.from(order);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDto get(Long id) {
        return PurchaseOrderDto.from(orders.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("PurchaseOrder", id)));
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderDto> list(Pageable pageable) {
        return orders.findAll(pageable).map(PurchaseOrderDto::from);
    }
}
