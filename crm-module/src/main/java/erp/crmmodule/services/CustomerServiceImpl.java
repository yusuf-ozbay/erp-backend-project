package erp.crmmodule.services;

import erp.commonmodule.exception.*; // 👈 ErrorCode, Business/Validation/ResourceNotFound
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
            // ❗ Artık business code + http status ErrorCode üstünden belirleniyor
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
     */
    @Override
    public List<CustomerDto> listCustomers(BigDecimal minBonus, BigDecimal maxBonus) {
        if (minBonus != null && maxBonus != null) {
            return customerMapper.toDtoList(customerRepository.findByBonusBetween(minBonus, maxBonus));
        }
        return customerMapper.toDtoList(customerRepository.findAll());
    }

    /**
     * Bonus ekleme
     * - Doküman: POST /api/customers/{id}/bonus
     * - Kural: amount > 0 olmalı (ValidationException)
     * - Audit: BonusTransaction kaydı atılır
     * - Bakiye: Negatif olamaz (BusinessException)
     */
    @Override
    @Transactional
    public CustomerDto addBonus(Long customerId, BonusRequestDto request) {
        // 1) Müşteri var mı? yoksa 404 + 1002
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));

        // 2) amount > 0 olmalı (doküman gereği)
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(ErrorCode.BONUS_NEGATIVE_OR_ZERO);
        }

        // 3) Bonus (line) kaydı
        BonusEntity bonus = new BonusEntity();
        bonus.setCustomer(customer);
        bonus.setAmount(request.getAmount());
        bonus.setDescription(request.getDescription());
        bonusRepository.save(bonus);

        // 4) Bakiye güncelle
        BigDecimal updatedBalance = customer.getBonus().add(request.getAmount());

        // 4.a) Ek güvenlik: bakiye asla < 0 olamaz (ileri reuse durumları için)
        if (updatedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.BONUS_BALANCE_NEGATIVE);
        }

        customer.setBonus(updatedBalance);
        customerRepository.save(customer);

        // 5) Audit kaydı (BonusTransaction)
        BonusTransactionEntity tx = new BonusTransactionEntity();
        tx.setCustomer(customer);
        tx.setAmount(request.getAmount());
        tx.setDescription("Bonus eklendi: " + request.getDescription());
        bonusTransactionRepository.save(tx);

        // 6) DTO dönüş
        return customerMapper.toDto(customer);
    }

    /**
     * Bonus hareket listesi
     * - Doküman: GET /api/customers/{id}/bonus-transactions
     * - Müşteri yoksa 404 döner.
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
}
