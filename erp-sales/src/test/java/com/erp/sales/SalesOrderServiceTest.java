package com.erp.sales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.erp.core.audit.AuditService;
import com.erp.core.event.SalesInvoicedEvent;
import com.erp.core.exception.BusinessRuleException;
import com.erp.inventory.service.InsufficientStockException;
import com.erp.inventory.service.ProductService;
import com.erp.sales.domain.Customer;
import com.erp.sales.domain.OrderStatus;
import com.erp.sales.domain.SalesOrder;
import com.erp.sales.domain.SalesOrderLine;
import com.erp.sales.dto.SalesOrderDto;
import com.erp.sales.repository.CustomerRepository;
import com.erp.sales.repository.SalesOrderRepository;
import com.erp.sales.service.SalesOrderService;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock SalesOrderRepository orders;
    @Mock CustomerRepository customers;
    @Mock ProductService productService;
    @Mock AuditService audit;
    @Mock ApplicationEventPublisher events;

    SalesOrderService service;
    SalesOrder order;

    @BeforeEach
    void setUp() {
        service = new SalesOrderService(orders, customers, productService, audit, events, Clock.systemUTC());
        Customer customer = new Customer();
        customer.setName("Acme");
        order = new SalesOrder();
        order.setCustomer(customer);
        order.setOrderDate(java.time.LocalDate.now());
        SalesOrderLine line = new SalesOrderLine();
        line.setProductId(7L);
        line.setProductName("Widget");
        line.setQuantity(2);
        line.setUnitPrice(new BigDecimal("50.00"));
        line.setLineTotal(new BigDecimal("100.00"));
        order.addLine(line);
    }

    @Test
    @DisplayName("Confirming a draft order deducts stock, marks it CONFIRMED and publishes an invoice event")
    void confirmHappyPath() {
        when(orders.findById(1L)).thenReturn(Optional.of(order));

        SalesOrderDto result = service.confirm(1L);

        assertEquals("CONFIRMED", result.status());
        verify(productService).adjustStock(7L, -2, "Sales order #1");
        verify(events).publishEvent(any(SalesInvoicedEvent.class));
    }

    @Test
    @DisplayName("If stock is insufficient the order stays DRAFT and no invoice event is published")
    void confirmInsufficientStock() {
        when(orders.findById(1L)).thenReturn(Optional.of(order));
        when(productService.adjustStock(anyLong(), anyInt(), anyString()))
                .thenThrow(new InsufficientStockException("SKU-1", 1, 2));

        assertThrows(InsufficientStockException.class, () -> service.confirm(1L));

        assertEquals(OrderStatus.DRAFT, order.getStatus());
        verify(events, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("A confirmed order cannot be confirmed or cancelled again")
    void confirmedOrderIsFinal() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orders.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessRuleException.class, () -> service.confirm(1L));
        assertThrows(BusinessRuleException.class, () -> service.cancel(1L));
    }

    @Test
    @DisplayName("A draft order can be cancelled without touching stock")
    void cancelDraft() {
        when(orders.findById(1L)).thenReturn(Optional.of(order));

        SalesOrderDto result = service.cancel(1L);

        assertEquals("CANCELLED", result.status());
        verify(productService, never()).adjustStock(anyLong(), anyInt(), anyString());
    }
}
