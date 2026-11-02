package com.erp.inventory.dto;

import com.erp.inventory.domain.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderDto(Long id, Long supplierId, String supplierName, String status, LocalDate orderDate,
                               BigDecimal totalAmount, List<Line> lines) {

    public record Line(Long productId, String sku, int quantity, BigDecimal unitCost) {
    }

    public static PurchaseOrderDto from(PurchaseOrder po) {
        List<Line> lines = po.getLines().stream()
                .map(l -> new Line(l.getProduct().getId(), l.getProduct().getSku(), l.getQuantity(), l.getUnitCost()))
                .toList();
        return new PurchaseOrderDto(po.getId(), po.getSupplier().getId(), po.getSupplier().getName(),
                po.getStatus().name(), po.getOrderDate(), po.totalAmount(), lines);
    }
}
