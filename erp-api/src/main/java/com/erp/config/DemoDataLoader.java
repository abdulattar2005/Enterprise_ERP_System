package com.erp.config;

import com.erp.hr.domain.Employee;
import com.erp.hr.repository.EmployeeRepository;
import com.erp.inventory.domain.Product;
import com.erp.inventory.domain.Supplier;
import com.erp.inventory.domain.Warehouse;
import com.erp.inventory.repository.ProductRepository;
import com.erp.inventory.repository.SupplierRepository;
import com.erp.inventory.repository.WarehouseRepository;
import com.erp.sales.domain.Customer;
import com.erp.sales.repository.CustomerRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Sample data so the API has something to show on first start. Only runs with the "dev" profile. */
@Component
@Profile("dev")
public class DemoDataLoader implements ApplicationRunner {

    private final EmployeeRepository employees;
    private final WarehouseRepository warehouses;
    private final SupplierRepository suppliers;
    private final ProductRepository products;
    private final CustomerRepository customers;

    public DemoDataLoader(EmployeeRepository employees, WarehouseRepository warehouses,
                          SupplierRepository suppliers, ProductRepository products, CustomerRepository customers) {
        this.employees = employees;
        this.warehouses = warehouses;
        this.suppliers = suppliers;
        this.products = products;
        this.customers = customers;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (employees.count() > 0 || products.count() > 0) {
            return;
        }
        employee("Asha", "Patil", "asha.patil@example.com", "Engineering", "Backend Developer", "60000");
        employee("Rohan", "Deshmukh", "rohan.deshmukh@example.com", "Sales", "Account Manager", "45000");
        employee("Meera", "Kulkarni", "meera.kulkarni@example.com", "Finance", "Accountant", "20000");

        Warehouse wh = new Warehouse();
        wh.setCode("WH-PUNE");
        wh.setName("Pune Main Warehouse");
        wh.setLocation("Pune, Maharashtra");
        warehouses.save(wh);

        Supplier supplier = new Supplier();
        supplier.setName("Sahyadri Components Pvt Ltd");
        supplier.setEmail("orders@sahyadri.example.com");
        suppliers.save(supplier);

        product("LAP-001", "Business Laptop", "Electronics", "55000.00", 25, 5, wh, supplier);
        product("MON-024", "24-inch Monitor", "Electronics", "9500.00", 40, 10, wh, supplier);
        product("KEY-010", "Wireless Keyboard", "Accessories", "1500.00", 8, 10, wh, supplier);
        product("CHR-200", "Ergonomic Chair", "Furniture", "12000.00", 3, 4, wh, supplier);

        Customer c = new Customer();
        c.setName("Bharat Traders");
        c.setEmail("purchase@bharattraders.example.com");
        c.setCompany("Bharat Traders");
        customers.save(c);
    }

    private void employee(String first, String last, String email, String dept, String title, String salary) {
        Employee e = new Employee();
        e.setFirstName(first);
        e.setLastName(last);
        e.setEmail(email);
        e.setDepartment(dept);
        e.setJobTitle(title);
        e.setMonthlySalary(new BigDecimal(salary));
        e.setHireDate(LocalDate.of(2025, 4, 1));
        employees.save(e);
    }

    private void product(String sku, String name, String category, String price, int stock, int reorder,
                         Warehouse wh, Supplier supplier) {
        Product p = new Product();
        p.setSku(sku);
        p.setName(name);
        p.setCategory(category);
        p.setUnitPrice(new BigDecimal(price));
        p.setStockQuantity(stock);
        p.setReorderLevel(reorder);
        p.setWarehouse(wh);
        p.setSupplier(supplier);
        products.save(p);
    }
}
