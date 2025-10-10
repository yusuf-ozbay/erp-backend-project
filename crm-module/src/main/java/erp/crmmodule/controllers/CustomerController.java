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

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;


    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(@RequestBody CustomerDto customerDto) {
        CustomerDto saved = customerService.createCustomer(customerDto);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDto>>> listCustomers(@RequestParam(required = false) BigDecimal minBonus, @RequestParam(required = false) BigDecimal maxBonus) {
        List<CustomerDto> customers = customerService.listCustomers(minBonus, maxBonus);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }


    @PostMapping("/{id}/bonus")
    public ResponseEntity<ApiResponse<CustomerDto>> addBonus(@PathVariable Long id, @RequestBody BonusRequestDto request) {
        CustomerDto updated = customerService.addBonus(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }


    @GetMapping("/{id}/bonus-transactions")
    public ResponseEntity<ApiResponse<List<BonusTransactionDto>>> listBonusTransactions(@PathVariable Long id) {
        List<BonusTransactionDto> transactions = customerService.listBonusTransactions(id);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}
