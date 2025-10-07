package erp.crmmodule.services;

import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.dto.BonusRequestDto;
import erp.crmmodule.dto.BonusTransactionDto;

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

    // === Invoice -> CRM "service→service" entegrasyonu için eklenenler ===

    /**
     * Müşteriyi ID ile getirir (404 davranışı üst katmanda verilebilir).
     * Invoice tarafı isterse kontrol amaçlı kullanır.
     */
    CustomerDto getById(Long customerId);

    /**
     * Bonus bakiyesine delta uygular ve BonusTransaction (audit) kaydı oluşturur.
     * - Satışta delta NEGATİF (ör. -200)
     * - İadede delta POZİTİF (ör. +50)
     * - Negatif bakiyeye düşme kontrolü burada yapılır (tek otorite CRM).
     */
    void applyBonusChange(Long customerId, BigDecimal delta, String description);
}
