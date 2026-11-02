package com.erp.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.erp.core.audit.AuditService;
import com.erp.core.exception.BusinessRuleException;
import com.erp.inventory.domain.Product;
import com.erp.inventory.dto.ProductDto;
import com.erp.inventory.repository.ProductRepository;
import com.erp.inventory.repository.SupplierRepository;
import com.erp.inventory.repository.WarehouseRepository;
import com.erp.inventory.service.InsufficientStockException;
import com.erp.inventory.service.ProductService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository products;
    @Mock WarehouseRepository warehouses;
    @Mock SupplierRepository suppliers;
    @Mock AuditService audit;

    ProductService service;
    Product product;

    @BeforeEach
    void setUp() {
        service = new ProductService(products, warehouses, suppliers, audit);
        product = new Product();
        product.setSku("SKU-1");
        product.setName("Widget");
        product.setUnitPrice(new BigDecimal("10.00"));
        product.setStockQuantity(10);
        product.setReorderLevel(3);
    }

    @Test
    @DisplayName("Adding stock increases the quantity and writes an audit entry")
    void addStock() {
        when(products.findById(1L)).thenReturn(Optional.of(product));
        when(products.saveAndFlush(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductDto result = service.adjustStock(1L, 5, "test");

        assertEquals(15, result.stockQuantity());
        verify(audit).log(any(), any());
    }

    @Test
    @DisplayName("Removing more stock than available is rejected and nothing is saved")
    void insufficientStock() {
        when(products.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> service.adjustStock(1L, -11, "test"));

        verify(products, never()).saveAndFlush(any(Product.class));
        assertEquals(10, product.getStockQuantity());
    }

    @Test
    @DisplayName("Removing exactly the available stock is allowed and flags the product as low stock")
    void drainToZero() {
        when(products.findById(1L)).thenReturn(Optional.of(product));
        when(products.saveAndFlush(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductDto result = service.adjustStock(1L, -10, "sale");

        assertEquals(0, result.stockQuantity());
        assertEquals(true, result.lowStock());
    }

    @Test
    @DisplayName("A zero adjustment is rejected")
    void zeroDelta() {
        assertThrows(BusinessRuleException.class, () -> service.adjustStock(1L, 0, "noop"));
    }
}
