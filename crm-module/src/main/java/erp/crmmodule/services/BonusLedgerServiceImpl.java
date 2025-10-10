package erp.crmmodule.services;

import erp.commonmodule.exception.BusinessException;
import erp.commonmodule.exception.ErrorCode;
import erp.commonmodule.exception.ValidationException;
import erp.crmmodule.dao.BonusTransactionDao;
import erp.crmmodule.dto.BonusTransactionDto;
import erp.crmmodule.mapper.BonusTransactionMapper;
import erp.crmmodule.models.BonusTransactionEntity;
import erp.crmmodule.models.CustomerEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BonusLedgerServiceImpl implements BonusLedgerService {

    // Yalnız ledger ile ilgili bağımlılıklar
    private final BonusTransactionDao bonusTransactionRepository;
    private final BonusTransactionMapper bonusTransactionMapper;

    // 👇 Customer var mı doğrulaması için SADECE Lookup port (küçük arayüz)
    private final CustomerLookupPort customerLookup;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public CustomerEntity addBonus(Long customerId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(ErrorCode.BONUS_NEGATIVE_OR_ZERO);
        }

        // Sadece varlık/doğrulama (DTO döner; yoksa 404)
        customerLookup.getById(customerId);

        // Managed referans
        CustomerEntity customerRef = em.getReference(CustomerEntity.class, customerId);

        // Delta = +amount
        return applyDeltaInternal(customerRef, amount,
                "Bonus eklendi: " + (description == null ? "" : description));
    }

    @Override
    @Transactional
    public CustomerEntity applyDelta(Long customerId, BigDecimal delta, String description) {
        // Doğrulama
        customerLookup.getById(customerId);

        // Managed referans
        CustomerEntity customerRef = em.getReference(CustomerEntity.class, customerId);

        // Negatif delta için bakiye kontrolü
        if (delta.signum() < 0 && customerRef.getBonus().compareTo(delta.abs()) < 0) {
            throw new BusinessException(ErrorCode.INVOICE_BONUS_INSUFFICIENT);
        }

        return applyDeltaInternal(customerRef, delta, (description == null ? "" : description));
    }

    @Override
    public List<BonusTransactionDto> listTransactions(Long customerId) {
        // İstenirse burada da customerLookup.getById(customerId) ile varlık doğrulaması ekleyebilirsin
        return bonusTransactionRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(bonusTransactionMapper::toDto)
                .toList();
    }

    // --- İç yardımcı ---
    private CustomerEntity applyDeltaInternal(CustomerEntity customer, BigDecimal delta, String description) {
        BigDecimal updated = customer.getBonus().add(delta);
        if (updated.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.BONUS_BALANCE_NEGATIVE);
        }
        customer.setBonus(updated); // managed entity -> flush ile yazılır

        BonusTransactionEntity tx = new BonusTransactionEntity();
        tx.setCustomer(customer);
        tx.setAmount(delta);
        tx.setDescription(description);
        bonusTransactionRepository.save(tx);

        return customer;
    }
}
