package erp.crmmodule.services;



import erp.commonmodule.exception.BusinessException;
import erp.crmmodule.dao.BonusDao;
import erp.crmmodule.dao.BonusTransactionDao;
import erp.crmmodule.dao.CustomerDao;
import erp.crmmodule.dto.BonusRequestDto;
import erp.crmmodule.dto.BonusTransactionDto;
import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.mapper.BonusTransactionMapper;
import erp.crmmodule.mapper.CustomerMapper;
import erp.crmmodule.models.BonusEntity;
import erp.crmmodule.models.BonusTransactionEntity;
import erp.crmmodule.models.CustomerEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDao customerRepository;
    private final BonusDao bonusRepository;
    private final BonusTransactionDao bonusTransactionRepository;
    private final CustomerMapper customerMapper;
    private final BonusTransactionMapper bonusTransactionMapper;

    /**
     * Yeni müşteri oluşturma
     * - Dokümanda: "POST /api/customers"
     * - Email benzersiz olmalı.
     */
    @Override
    public CustomerDto createCustomer(CustomerDto customerDto) {
        if (customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new BusinessException(1001, "Email zaten kayıtlı");
        }
        CustomerEntity entity = customerMapper.toEntity(customerDto);
        entity.setBonusBalance(BigDecimal.ZERO); // başlangıç bonusu sıfır
        CustomerEntity saved = customerRepository.save(entity);
        return customerMapper.toDto(saved);
    }

    /**
     * Müşteri listesini getirir
     * - Dokümanda: "GET /api/customers"
     * - minBonus / maxBonus filtreleri opsiyonel olacak.
     */
    @Override
    public List<CustomerDto> listCustomers(BigDecimal minBonus, BigDecimal maxBonus) {
        if (minBonus != null && maxBonus != null) {
            return customerMapper.toDtoList(customerRepository.findByBonusBalanceBetween(minBonus, maxBonus));
        }
        return customerMapper.toDtoList(customerRepository.findAll());
    }

    /**
     * Müşteriye bonus ekler
     * - Dokümanda: "POST /api/customers/{id}/bonus"
     * - Bonus tablosuna kayıt açılır
     * - Customer bonus_balance güncellenir
     * - BonusTransaction kaydı oluşturulur
     */
    @Override
    @Transactional
    public CustomerDto addBonus(Long customerId, BonusRequestDto request) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(1002, "Müşteri bulunamadı"));

        // Bonus kaydı
        BonusEntity bonus = new BonusEntity();
        bonus.setCustomer(customer);
        bonus.setAmount(request.getAmount());
        bonus.setDescription(request.getDescription());
        bonusRepository.save(bonus);

        // Customer bonus güncelle
        BigDecimal updatedBalance = customer.getBonusBalance().add(request.getAmount());
        customer.setBonusBalance(updatedBalance);
        customerRepository.save(customer);

        // BonusTransaction kaydı
        BonusTransactionEntity tx = new BonusTransactionEntity();
        tx.setCustomer(customer);
        tx.setAmount(request.getAmount());
        tx.setDescription("Bonus eklendi: " + request.getDescription());
        bonusTransactionRepository.save(tx);

        return customerMapper.toDto(customer);
    }

    /**
     * Müşteriye ait bonus transaction listesini getirir
     * - Dokümanda: "GET /api/customers/{id}/bonus-transactions"
     */
    @Override
    public List<BonusTransactionDto> listBonusTransactions(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException(1003, "Müşteri bulunamadı");
        }
        return bonusTransactionMapper.toDtoList(
                bonusTransactionRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId)
        );
    }
}
