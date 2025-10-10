package erp.crmmodule.services;

import erp.commonmodule.exception.ErrorCode;
import erp.commonmodule.exception.ValidationException;
import erp.crmmodule.dao.CustomerDao;
import erp.crmmodule.dto.BonusRequestDto;
import erp.crmmodule.dto.BonusTransactionDto;
import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.mapper.CustomerMapper;
import erp.crmmodule.models.CustomerEntity;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDao customerDao;
    private final CustomerMapper customerMapper;
    private final BonusLedgerService bonusLedgerService;

    @Override
    public CustomerDto createCustomer(CustomerDto customerDto) {

        if (customerDao.existsByEmail(customerDto.getEmail())) {
            throw new ValidationException(ErrorCode.CUSTOMER_EMAIL_EXISTS);
        }

        CustomerEntity entity = customerMapper.toEntity(customerDto);
        entity.setBonus(BigDecimal.ZERO);
        CustomerEntity saved = customerDao.save(entity);
        return customerMapper.toDto(saved);
    }

    @Override
    public List<CustomerDto> listCustomers(BigDecimal minBonus, BigDecimal maxBonus) {
        // Dinamik filtre (Specification)
        //Bonus aralığı isteğe bağlı; gelen parametreye göre dinamik filtre kuruyoruz.
        Specification<CustomerEntity> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (minBonus != null) preds.add(cb.greaterThanOrEqualTo(root.get("bonus"), minBonus));
            if (maxBonus != null) preds.add(cb.lessThanOrEqualTo(root.get("bonus"), maxBonus));
            return cb.and(preds.toArray(new Predicate[0]));
        };

        return customerDao.findAll(spec)
                .stream()
                .map(customerMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CustomerDto addBonus(Long customerId, BonusRequestDto request) {
        CustomerEntity updated = bonusLedgerService.addBonus(customerId, request.getAmount(), request.getDescription());
        return customerMapper.toDto(updated);
    }

    @Override
    public List<BonusTransactionDto> listBonusTransactions(Long customerId) {
        return bonusLedgerService.listTransactions(customerId);
    }


    @Override
    @Transactional
    public void applyBonusChange(Long customerId, BigDecimal delta, String description) {
        bonusLedgerService.applyDelta(customerId, delta, description);
    }
}
