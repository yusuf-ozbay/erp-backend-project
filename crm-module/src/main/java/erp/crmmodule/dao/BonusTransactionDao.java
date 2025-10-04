package erp.crmmodule.dao;

import erp.crmmodule.models.BonusTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BonusTransactionDao extends JpaRepository<BonusTransactionEntity, Long> {
    List<BonusTransactionEntity> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
}
