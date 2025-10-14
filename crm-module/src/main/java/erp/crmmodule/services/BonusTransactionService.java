package erp.crmmodule.services;

import erp.crmmodule.dto.BonusTransactionDto;
import erp.crmmodule.models.CustomerEntity;

import java.math.BigDecimal;
import java.util.List;


public interface BonusTransactionService {


    List<BonusTransactionDto> listTransactions(Long customerId);

    void save(CustomerEntity customer, BigDecimal delta, String description);
}
