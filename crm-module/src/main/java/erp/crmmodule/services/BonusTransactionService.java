package erp.crmmodule.services;

import erp.crmmodule.dto.BonusTransactionDto;
import erp.crmmodule.models.CustomerEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * Bonus defteri (ledger) arayüzü:
 * - addBonus: Pozitif miktarda bonus ekler (delta>0)
 * - applyDelta: Negatif/pozitif delta uygulayarak bakiyeyi günceller
 * - listTransactions: Müşteriye ait hareketleri döner
 *
 * NOT: Bu arayüz, dış dünyaya "Bonus operasyonları"nı sunar.
 *      İçeride hangi repository'ler kullanıldığı gizlidir (DIP).
 */
public interface BonusTransactionService {

    /**
     * Pozitif bonus ekleme (kampanya, manuel yükleme vb.)
     * @param customerId Müşteri kimliği
     * @param amount     Pozitif tutar
     * @param description Açıklama (UI/rapor için)
     * @return Güncellenmiş CustomerEntity (bakiye güncellenmiş hali)
     */
    //CustomerEntity addBonus(Long customerId, BigDecimal amount, String description);

    /**
     * Delta uygulama:
     * - Satış: delta < 0 (bonus harcanır)
     * - İade:  delta > 0 (bonus iade edilir)
     * @param customerId Müşteri kimliği
     * @param delta      Negatif/pozitif tutar
     * @param description Açıklama
     * @return Güncellenmiş CustomerEntity
     */
    //CustomerEntity applyDelta(Long customerId, BigDecimal delta, String description);

    /**
     * Bonus hareket listesini döner.
     * @param customerId Müşteri kimliği
     * @return BonusTransactionDto listesi (tarih, miktar, açıklama)
     */
    List<BonusTransactionDto> listTransactions(Long customerId);

    void save(CustomerEntity customer, BigDecimal delta, String description);
}
