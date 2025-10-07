package erp.crmmodule.services;

import erp.commonmodule.exception.*;
import erp.commonmodule.exception.ErrorCode;
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
     * - Doküman: POST /api/customers
     * - Kural: Email benzersiz olmalı (dup ise ValidationException).
     */
    @Override
    public CustomerDto createCustomer(CustomerDto customerDto) {
        if (customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new ValidationException(ErrorCode.CUSTOMER_EMAIL_EXISTS);
        }
        CustomerEntity entity = customerMapper.toEntity(customerDto);
        entity.setBonus(BigDecimal.ZERO); // başlangıç bonusu 0
        CustomerEntity saved = customerRepository.save(entity);
        return customerMapper.toDto(saved);
    }

    /**
     * Müşteri listesi (+ opsiyonel min/max bonus filtresi)
     * - Doküman: GET /api/customers
     * - İyileştirme: min veya max TEK BAŞINA da gelebilir.
     */
    @Override
    public List<CustomerDto> listCustomers(BigDecimal minBonus, BigDecimal maxBonus) {
        if (minBonus != null && maxBonus != null) {
            return customerMapper.toDtoList(customerRepository.findByBonusBetween(minBonus, maxBonus));
        } else if (minBonus != null) {
            return customerMapper.toDtoList(customerRepository.findByBonusBetween(minBonus, new BigDecimal("999999999999")));
        } else if (maxBonus != null) {
            return customerMapper.toDtoList(customerRepository.findByBonusBetween(BigDecimal.ZERO, maxBonus));
        }
        return customerMapper.toDtoList(customerRepository.findAll());
    }

    /**
     * Bonus ekleme (dokümandaki bonus tanımlama akışı)
     * - POST /api/customers/{id}/bonus
     * - Kural: amount > 0 olmalı (ValidationException)
     * - Bonus tablosuna line kaydı + bakiye güncelle + audit (pozitif)
     */
    @Override
    @Transactional
    public CustomerDto addBonus(Long customerId, BonusRequestDto request) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(ErrorCode.BONUS_NEGATIVE_OR_ZERO);
        }

        // Bonus (line) kaydı
        BonusEntity bonus = new BonusEntity();
        bonus.setCustomer(customer);
        bonus.setAmount(request.getAmount());
        bonus.setDescription(request.getDescription());
        bonusRepository.save(bonus);

        // Bakiye + audit
        applyBonusDeltaInternal(customer, request.getAmount(), "Bonus eklendi: " + request.getDescription());

        return customerMapper.toDto(customer);
    }

    /**
     * Bonus hareket listesi
     * - GET /api/customers/{id}/bonus-transactions
     */
    @Override
    public List<BonusTransactionDto> listBonusTransactions(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        return bonusTransactionMapper.toDtoList(
                bonusTransactionRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId)
        );
    }

    // ====== Invoice → CRM service→service entegrasyonu için eklenenler ======

    /**
     * Müşteriyi ID ile getir (DTO).
     * - NotFound kontrolünü burada veya üst katta verebilirsin. Burada veriyoruz.
     */
    @Override
    public CustomerDto getById(Long customerId) {
        CustomerEntity c = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));
        return customerMapper.toDto(c);
    }

    /**
     * Bonus bakiyesine delta uygular ve audit kaydı atar.
     * - Satış: delta NEGATİF, İade: delta POZİTİF
     * - Yetersiz bakiye kontrolü burada yapılır (tek otorite CRM)
     */
    @Override
    @Transactional
    public void applyBonusChange(Long customerId, BigDecimal delta, String description) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));

        // Satışta delta negatif geleceği için "yetersiz bakiye" burada yakalanır
        if (delta.signum() < 0 && customer.getBonus().compareTo(delta.abs()) < 0) {
            throw new BusinessException(ErrorCode.INVOICE_BONUS_INSUFFICIENT);
        }

        applyBonusDeltaInternal(customer, delta, description);
    }

    /**
     * İç yardımcı:
     * - Gerçek bakiyeyi günceller
     * - Negatif bakiye koruması
     * - BonusTransaction (audit) atar (delta işaretli kaydedilir)
     */
    private void applyBonusDeltaInternal(CustomerEntity customer, BigDecimal delta, String description) {
        BigDecimal updated = customer.getBonus().add(delta);
        if (updated.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.BONUS_BALANCE_NEGATIVE);
        }
        customer.setBonus(updated);
        customerRepository.save(customer);

        BonusTransactionEntity tx = new BonusTransactionEntity();
        tx.setCustomer(customer);
        tx.setAmount(delta); // 🔴 satışta negatif, iadede pozitif — doküman senaryosuna birebir uyum
        tx.setDescription(description);
        bonusTransactionRepository.save(tx);
    }
}
