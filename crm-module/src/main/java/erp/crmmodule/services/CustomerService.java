package erp.crmmodule.services;

import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.dto.BonusRequestDto;
import erp.crmmodule.dto.BonusTransactionDto;
import erp.crmmodule.models.CustomerEntity;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * CustomerService
 * - Dokümandaki CRM operasyonlarını tanımlar.
 * - NOT: Invoice modülü, DAO’ya dokunmak yerine bu arayüz üzerinden CRM ile konuşur.
 */
public interface CustomerService {

    // === Dokümandaki mevcut uçların karşılığı ===
    CustomerDto createCustomer(CustomerDto customerDto);
    List<CustomerDto> listCustomers(BigDecimal minBonus, BigDecimal maxBonus);
    CustomerDto addBonus(Long customerId, BonusRequestDto request);
    List<BonusTransactionDto> listBonusTransactions(Long customerId);

    void applyBonusChange(Long customerId, BigDecimal delta, String description);

    CustomerEntity addBonus(Long customerId, BigDecimal amount, String description);

    void applyDelta(Long customerId, BigDecimal delta, String description);


}
