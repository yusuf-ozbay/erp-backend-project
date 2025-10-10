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


/**
 * Bonusun “tek doğruluk noktası”.
 * Burada iki şey yapılır:
 * 1) Müşteri bakiyesini güncelle (ekle/harca/iade → delta mantığı)
 * 2) Her değişiklik için hareket (ledger) kaydı at
 * Amaç: Bonus mantığı farklı yerlerde kopyalanmasın, tek yerden yönetilsin.
 */

@Service
@RequiredArgsConstructor
public class BonusLedgerServiceImpl implements BonusLedgerService {

    private final BonusTransactionDao bonusTransactionDao;
    private final BonusTransactionMapper bonusTransactionMapper;

    private final CustomerLookupPort customerLookup;

    @PersistenceContext
    private EntityManager em;


    @Override
    @Transactional
    public CustomerEntity addBonus(Long customerId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(ErrorCode.BONUS_NEGATIVE_OR_ZERO);
        }
        customerLookup.getById(customerId);

        // Sadece id ile referans alıp çalışmak yeterli (performans + sade ilişki).
        CustomerEntity customerRef = em.getReference(CustomerEntity.class, customerId);
        return applyDeltaInternal(customerRef, amount,
                "Bonus eklendi: " + (description == null ? "" : description));
    }


    @Override
    @Transactional
    public CustomerEntity applyDelta(Long customerId, BigDecimal delta, String description) {
        customerLookup.getById(customerId);

        CustomerEntity customerRef = em.getReference(CustomerEntity.class, customerId);
        if (delta.signum() < 0 && customerRef.getBonus().compareTo(delta.abs()) < 0) {
            throw new BusinessException(ErrorCode.INVOICE_BONUS_INSUFFICIENT);
        }

        return applyDeltaInternal(customerRef, delta, (description == null ? "" : description));
    }


    @Override
    public List<BonusTransactionDto> listTransactions(Long customerId) {

        return bonusTransactionDao.findByCustomer_IdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(bonusTransactionMapper::toDto)
                .toList();
    }


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
        bonusTransactionDao.save(tx);

        return customer;
    }
}
