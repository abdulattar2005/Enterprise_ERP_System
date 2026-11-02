package com.erp.sales.repository;

import com.erp.sales.domain.OrderStatus;
import com.erp.sales.domain.SalesOrder;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    long countByStatus(OrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from SalesOrder o where o.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") OrderStatus status);
}
