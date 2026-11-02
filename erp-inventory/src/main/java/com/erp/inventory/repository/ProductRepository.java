package com.erp.inventory.repository;

import com.erp.inventory.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

    long countByActiveTrue();

    @Query("select p from Product p where p.active = true and p.stockQuantity <= p.reorderLevel order by p.stockQuantity")
    List<Product> findLowStock();

    @Query("select coalesce(sum(p.stockQuantity * p.unitPrice), 0) from Product p where p.active = true")
    BigDecimal inventoryValuation();
}
