package erp.crmmodule.controllers;

import erp.commonmodule.response.ApiResponse;
import erp.crmmodule.dto.BonusRequestDto;
import erp.crmmodule.dto.BonusTransactionDto;
import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * CustomerController
 * - Dokümanda belirtilen REST API uçlarını expose eden katman.
 * - Controller'da Entity değil, sadece DTO'lar kullanılıyor (doküman şartı).
 * - Response formatı ApiResponse<T> (dokümandaki generic response yapısı).
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 1) Yeni müşteri kaydetme
     * Endpoint: POST /api/customers
     * RequestBody: CustomerDto
     * Response: ApiResponse<CustomerDto>
     *
     * Dokümandaki karşılığı:
     * "Müşteri: POST /api/customers"
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(@RequestBody CustomerDto customerDto) {
        CustomerDto saved = customerService.createCustomer(customerDto);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    /**
     * 2) Müşteri listesi getirme
     * Endpoint: GET /api/customers?minBonus=&maxBonus=
     * Query params opsiyonel.
     * Response: ApiResponse<List<CustomerDto>>
     *
     * Dokümandaki karşılığı:
     * "Müşteri Listesi: API çağrıldığında kalan bonus gösterilecek, opsiyonel filtreler (minBonus/maxBonus)."
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDto>>> listCustomers(
            @RequestParam(required = false) BigDecimal minBonus,
            @RequestParam(required = false) BigDecimal maxBonus) {
        List<CustomerDto> customers = customerService.listCustomers(minBonus, maxBonus);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    /**
     * 3) Müşteriye bonus ekleme
     * Endpoint: POST /api/customers/{id}/bonus
     * RequestBody: BonusRequestDto
     * Response: ApiResponse<CustomerDto>
     *
     * Dokümandaki karşılığı:
     * "Bonus Ekle: POST /api/customers/1/bonus { 'amount': 500, 'description': 'Yeni kampanya bonusu' }"
     */
    @PostMapping("/{id}/bonus")
    public ResponseEntity<ApiResponse<CustomerDto>> addBonus(
            @PathVariable Long id,
            @RequestBody BonusRequestDto request) {
        CustomerDto updated = customerService.addBonus(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 4) Bonus hareketlerini listeleme
     * Endpoint: GET /api/customers/{id}/bonus-transactions
     * Response: ApiResponse<List<BonusTransactionDto>>
     *
     * Dokümandaki karşılığı:
     * "BonusTransaction: GET /api/customers/{id}/bonus-transactions"
     */
    @GetMapping("/{id}/bonus-transactions")
    public ResponseEntity<ApiResponse<List<BonusTransactionDto>>> listBonusTransactions(
            @PathVariable Long id) {
        List<BonusTransactionDto> transactions = customerService.listBonusTransactions(id);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}
