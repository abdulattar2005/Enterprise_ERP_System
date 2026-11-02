package com.erp;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Black-box tests through the real HTTP layer, security filter chain, JPA and the in-memory H2 database.
 * The main test walks a full business cycle: purchase -> stock -> sale -> ledger.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErpEndToEndTest {

    @Autowired MockMvc mvc;

    private String login(String user, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + user + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.accessToken");
    }

    private long idOf(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private String postJson(String url, String token, String json) throws Exception {
        return mvc.perform(post(url).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @DisplayName("Requests without a token are rejected with 401")
    void noTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/hr/employees")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Wrong password is rejected with 401")
    void wrongPassword() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("A plain USER cannot read HR data (403)")
    void userCannotReadEmployees() throws Exception {
        String token = login("user", "user123");
        mvc.perform(get("/api/hr/employees").header("Authorization", token)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Validation errors return 400 with field details")
    void validationErrors() throws Exception {
        String token = login("admin", "admin123");
        mvc.perform(post("/api/hr/employees").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"firstName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    @DisplayName("Duplicate employee email is rejected with 422")
    void duplicateEmail() throws Exception {
        String token = login("admin", "admin123");
        String json = "{\"firstName\":\"Dup\",\"lastName\":\"Test\",\"email\":\"dup.test@example.com\","
                + "\"monthlySalary\":30000,\"hireDate\":\"2025-01-15\"}";
        postJson("/api/hr/employees", token, json);
        mvc.perform(post("/api/hr/employees").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Full cycle: receive purchase, sell stock, ledger stays balanced, overselling is refused")
    void purchaseToSaleCycle() throws Exception {
        String admin = login("admin", "admin123");

        long supplierId = idOf(postJson("/api/inventory/suppliers", admin,
                "{\"name\":\"E2E Supplier\",\"email\":\"e2e@supplier.example.com\"}"));
        long productId = idOf(postJson("/api/inventory/products", admin,
                "{\"sku\":\"E2E-1\",\"name\":\"E2E Product\",\"category\":\"Test\",\"unitPrice\":100.00,"
                        + "\"stockQuantity\":0,\"reorderLevel\":2,\"supplierId\":" + supplierId + "}"));

        // purchase 10 units at 60.00 each and receive them
        long poId = idOf(postJson("/api/inventory/purchase-orders", admin,
                "{\"supplierId\":" + supplierId + ",\"lines\":[{\"productId\":" + productId
                        + ",\"quantity\":10,\"unitCost\":60.00}]}"));
        mvc.perform(post("/api/inventory/purchase-orders/" + poId + "/receive").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"));
        mvc.perform(get("/api/inventory/products/" + productId).header("Authorization", admin))
                .andExpect(jsonPath("$.stockQuantity").value(10));

        // a second receive of the same PO must fail
        mvc.perform(post("/api/inventory/purchase-orders/" + poId + "/receive").header("Authorization", admin))
                .andExpect(status().isUnprocessableEntity());

        // sell 4 units
        long customerId = idOf(postJson("/api/sales/customers", admin,
                "{\"name\":\"E2E Customer\",\"email\":\"e2e@customer.example.com\"}"));
        long orderId = idOf(postJson("/api/sales/orders", admin,
                "{\"customerId\":" + customerId + ",\"lines\":[{\"productId\":" + productId + ",\"quantity\":4}]}"));
        mvc.perform(post("/api/sales/orders/" + orderId + "/confirm").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(400.0));
        mvc.perform(get("/api/inventory/products/" + productId).header("Authorization", admin))
                .andExpect(jsonPath("$.stockQuantity").value(6));

        // trying to sell more than is left is refused and leaves stock untouched
        long bigOrderId = idOf(postJson("/api/sales/orders", admin,
                "{\"customerId\":" + customerId + ",\"lines\":[{\"productId\":" + productId + ",\"quantity\":7}]}"));
        mvc.perform(post("/api/sales/orders/" + bigOrderId + "/confirm").header("Authorization", admin))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/inventory/products/" + productId).header("Authorization", admin))
                .andExpect(jsonPath("$.stockQuantity").value(6));

        // the double-entry ledger must still balance after all of the above
        mvc.perform(get("/api/accounting/trial-balance").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanced").value(true));

        mvc.perform(get("/api/reports/dashboard").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ledgerBalanced").value(true));
    }

    @Test
    @DisplayName("Payroll run is idempotent and posts a balanced ledger entry")
    void payrollRun() throws Exception {
        String admin = login("admin", "admin123");
        postJson("/api/hr/employees", admin,
                "{\"firstName\":\"Pay\",\"lastName\":\"Roll\",\"email\":\"pay.roll@example.com\","
                        + "\"monthlySalary\":50000,\"hireDate\":\"2025-02-01\"}");

        String first = mvc.perform(post("/api/hr/payroll/2026-09/run").header("Authorization", admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        int count = ((java.util.List<?>) JsonPath.read(first, "$")).size();

        mvc.perform(post("/api/hr/payroll/2026-09/run").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(count)));

        mvc.perform(get("/api/accounting/trial-balance").header("Authorization", admin))
                .andExpect(jsonPath("$.balanced").value(true));
    }

    @Test
    @DisplayName("Invalid payroll period is rejected")
    void invalidPeriod() throws Exception {
        String admin = login("admin", "admin123");
        mvc.perform(post("/api/hr/payroll/not-a-period/run").header("Authorization", admin))
                .andExpect(status().isUnprocessableEntity());
    }
}
