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
}
