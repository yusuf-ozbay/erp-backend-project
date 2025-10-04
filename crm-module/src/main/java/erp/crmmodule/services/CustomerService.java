package erp.crmmodule.services;

import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.dto.BonusRequestDto;
import erp.crmmodule.dto.BonusTransactionDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * CustomerService
 * - Dokümanda beklenen operasyonları tanımlar:
 *   1. Müşteri ekleme
 *   2. Müşteri listeleme (min/max bonus filtresiyle)
 *   3. Müşteriye bonus ekleme
 *   4. Bonus hareketlerini listeleme
 */
public interface CustomerService {

    CustomerDto createCustomer(CustomerDto customerDto);

    List<CustomerDto> listCustomers(BigDecimal minBonus, BigDecimal maxBonus);

    CustomerDto addBonus(Long customerId, BonusRequestDto request);

    List<BonusTransactionDto> listBonusTransactions(Long customerId);
}
