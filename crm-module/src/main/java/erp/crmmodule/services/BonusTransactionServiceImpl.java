package erp.crmmodule.services;

import erp.crmmodule.dao.BonusTransactionDao;
import erp.crmmodule.dto.BonusTransactionDto;
import erp.crmmodule.mapper.BonusTransactionMapper;
import erp.crmmodule.models.BonusTransactionEntity;
import erp.crmmodule.models.CustomerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


/**
 * Bonusun “tek doğruluk noktası”.
 * Burada iki şey yapılır:
 * 1) Müşteri bakiyesini güncelle (ekle/harca/iade → delta mantığı)
 * 2) Her değişiklik için hareket (Transaction) kaydı at
 * Amaç: Bonus mantığı farklı yerlerde kopyalanmasın, tek yerden yönetilsin.
 */

@Service
@RequiredArgsConstructor
public class BonusTransactionServiceImpl implements BonusTransactionService {

    private final BonusTransactionDao bonusTransactionDao;
    private final BonusTransactionMapper bonusTransactionMapper;


    @Override
    public List<BonusTransactionDto> listTransactions(Long customerId) {

        return bonusTransactionDao.findByCustomer_IdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(bonusTransactionMapper::toDto)
                .toList();
    }


    @Override
    public void save(CustomerEntity customer, BigDecimal delta, String description) {
        BonusTransactionEntity bonusTransaction = new BonusTransactionEntity();
        bonusTransaction.setCustomer(customer);
        bonusTransaction.setAmount(delta);
        bonusTransaction.setDescription(description);
        bonusTransactionDao.save(bonusTransaction);
    }


}
