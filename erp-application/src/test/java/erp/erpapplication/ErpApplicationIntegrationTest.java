package erp.erpapplication;

import erp.commonmodule.response.ApiResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FULL INTEGRATION TEST (PostgreSQL)
 * -------------------------------------------------
 * Bu test, tüm uygulamayı (ErpApplication) gerçek PostgreSQL
 * üzerinde ayağa kaldırır ve Swagger dokümanındaki uçtan uca
 * senaryoyu birebir test eder.
 */
@SpringBootTest(
        classes = ErpApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ErpApplicationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // 🔹 1️⃣ Müşteri oluştur
    @Test
    @Order(1)
    void shouldCreateCustomer() {
        Map<String, Object> customer = Map.of(
                "name", "Ali Veli",
                "email", "ali@erp.com"
        );

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl("/api/customers"), customer, ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getOutcome_type()).isEqualTo("success");
    }

    // 🔹 2️⃣ Bonus ekle
    @Test
    @Order(2)
    void shouldAddBonusToCustomer() {
        Map<String, Object> bonus = Map.of(
                "amount", 500,
                "description", "Hoşgeldin kampanyası"
        );

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl("/api/customers/1/bonus"), bonus, ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getOutcome_type()).isEqualTo("success");
    }

    // 🔹 3️⃣ Satış faturası kes (bonus düşer)
    @Test
    @Order(3)
    void shouldCreateRetailSaleInvoice() {
        Map<String, Object> invoice = Map.of(
                "customerId", 1,
                "type", "RETAIL_SALE",
                "amount", 200,
                "lines", List.of(Map.of("productId", 1, "quantity", 2, "price", 100))
        );

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl("/api/invoices"), invoice, ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getOutcome_type()).isEqualTo("success");
    }

    // 🔹 4️⃣ İade faturası kes (bonus geri gelir)
    @Test
    @Order(4)
    void shouldCreateRetailReturnInvoice() {
        Map<String, Object> invoice = Map.of(
                "customerId", 1,
                "type", "RETAIL_RETURN",
                "amount", 50,
                "lines", List.of(Map.of("productId", 2, "quantity", 1, "price", 50))
        );

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl("/api/invoices"), invoice, ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getOutcome_type()).isEqualTo("success");
    }

    // 🔹 5️⃣ Bonus bakiyesi kontrolü
    @Test
    @Order(5)
    void shouldShowCorrectBonusBalance() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                baseUrl("/api/customers"), ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getOutcome_type()).isEqualTo("success");

        // JSON içinde "bonus": 350 arıyoruz
        assertThat(response.getBody().getData().toString()).contains("350");
    }

    // 🔹 6️⃣ Bonus hareket geçmişi
    @Test
    @Order(6)
    void shouldListBonusTransactions() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                baseUrl("/api/customers/1/bonus-transactions"), ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getOutcome_type()).isEqualTo("success");

        // 3 adet hareket beklenir (+500, -200, +50)
        assertThat(response.getBody().getData().toString()).contains("Hoşgeldin");
        assertThat(response.getBody().getData().toString()).contains("Bonus harcandı");
        assertThat(response.getBody().getData().toString()).contains("Bonus iade");
    }

    // 🔹 7️⃣ Negatif bonus hatası
    @Test
    @Order(7)
    void shouldRejectNegativeBonus() {
        Map<String, Object> bonus = Map.of(
                "amount", -100,
                "description", "Test negatif"
        );

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl("/api/customers/1/bonus"), bonus, ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }



    // 🔹 8️⃣ 0 tutarlı fatura reddi (<=0 yasak)
    @Test
    @Order(8)
    void shouldRejectZeroAmountInvoice() {
        Map<String, Object> invoice = Map.of(
                "customerId", 1,
                "type", "RETAIL_SALE",
                "amount", 0, // <= 0 yasak
                "lines", List.of(Map.of("productId", 3, "quantity", 1, "price", 0))
        );

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl("/api/invoices"), invoice, ApiResponse.class
        );

        // GlobalExceptionHandler ValidationException'ı 400 olarak döndürüyor
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getOutcome_type()).isEqualTo("error");
    }

    // 🔹 9️⃣ Yetersiz bakiye ile satış faturası (insufficient balance)
    @Test
    @Order(9)
    void shouldRejectWhenBonusInsufficient() {
        // Mevcut bakiye 350; 1000 harcatmaya çalışalım
        Map<String, Object> invoice = Map.of(
                "customerId", 1,
                "type", "RETAIL_SALE",
                "amount", 1000,
                "lines", List.of(Map.of("productId", 4, "quantity", 10, "price", 100))
        );

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl("/api/invoices"), invoice, ApiResponse.class
        );

        // BusinessException(INVOICE_BONUS_INSUFFICIENT) => 400
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getOutcome_type()).isEqualTo("error");
    }

    // 🔹 🔟 Geçersiz fatura tipi
    @Test
    @Order(10)
    void shouldRejectInvalidInvoiceType() {
        Map<String, Object> invoice = Map.of(
                "customerId", 1,
                "type", "NOT_A_REAL_TYPE",
                "amount", 10,
                "lines", List.of(Map.of("productId", 5, "quantity", 1, "price", 10))
        );

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl("/api/invoices"), invoice, ApiResponse.class
        );

        // BusinessException(INVOICE_INVALID_TYPE) => 400
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getOutcome_type()).isEqualTo("error");
    }

    // 🔹 1️⃣1️⃣ Var olmayan müşteriye bonus ekleme (404)
    @Test
    @Order(11)
    void shouldReturn404WhenCustomerNotFoundOnBonus() {
        Map<String, Object> bonus = Map.of(
                "amount", 100,
                "description", "ghost user"
        );

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl("/api/customers/999/bonus"), bonus, ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getOutcome_type()).isEqualTo("error");
    }

    // 🔹 1️⃣2️⃣ Müşteri listesinde min/max filtre (Specification testi)
    @Test
    @Order(12)
    void shouldFilterCustomersByMinMaxBonus() {
        // min=300 → Ali (350) listede olmalı
        ResponseEntity<ApiResponse> respMin = restTemplate.getForEntity(
                baseUrl("/api/customers?minBonus=300"), ApiResponse.class
        );
        assertThat(respMin.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respMin.getBody().getData().toString()).contains("Ali Veli");

        // max=100 → Ali (350) olmamalı
        ResponseEntity<ApiResponse> respMax = restTemplate.getForEntity(
                baseUrl("/api/customers?maxBonus=100"), ApiResponse.class
        );
        assertThat(respMax.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respMax.getBody().getData().toString()).doesNotContain("Ali Veli");

        // min=200&max=400 → Ali (350) listede
        ResponseEntity<ApiResponse> respRange = restTemplate.getForEntity(
                baseUrl("/api/customers?minBonus=200&maxBonus=400"), ApiResponse.class
        );
        assertThat(respRange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respRange.getBody().getData().toString()).contains("Ali Veli");
    }



}
