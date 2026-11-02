package com.erp.sales.dto;

import com.erp.sales.domain.SalesOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesOrderDto(Long id, Long customerId, String customerName, String status, LocalDate orderDate,
                            BigDecimal totalAmount, List<Line> lines) {

    public record Line(Long productId, String productName, int quantity, BigDecimal unitPrice,
                       BigDecimal lineTotal) {
    }

    public static SalesOrderDto from(SalesOrder o) {
        List<Line> lines = o.getLines().stream()
                .map(l -> new Line(l.getProductId(), l.getProductName(), l.getQuantity(), l.getUnitPrice(),
                        l.getLineTotal()))
                .toList();
        return new SalesOrderDto(o.getId(), o.getCustomer().getId(), o.getCustomer().getName(),
                o.getStatus().name(), o.getOrderDate(), o.getTotalAmount(), lines);
    }
}
