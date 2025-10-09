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
import jakarta.persistence.criteria.Predicate;               // 👈 Specification için
import org.springframework.data.jpa.domain.Specification;   // 👈 Specification için
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
     * Müşteri listesi – Specification ile opsiyonel min/max filtreleri.
     * - if/else karmaşasını kaldırır, ileride yeni filtre eklemek kolaylaşır.
     */
    @Override
    public List<CustomerDto> listCustomers(BigDecimal minBonus, BigDecimal maxBonus) {

        // Dinamik predicate listesi oluştur
        Specification<CustomerEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // minBonus varsa: bonus >= minBonus
            if (minBonus != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bonus"), minBonus));
            }
            // maxBonus varsa: bonus <= maxBonus
            if (maxBonus != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("bonus"), maxBonus));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Repo’dan entity listesi → stream ile DTO’ya mapleme
        return customerRepository.findAll(spec)
                .stream()
                .map(customerMapper::toDto)   // MapStruct tekil mapper
                .toList();
    }

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

        // Repo’dan entity listesi → stream ile DTO’ya mapleme
        return bonusTransactionRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(bonusTransactionMapper::toDto)  // MapStruct tekil mapper
                .toList();
    }

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
        tx.setAmount(delta);      // satışta negatif, iadede pozitif
        tx.setDescription(description);
        bonusTransactionRepository.save(tx);
    }
}
