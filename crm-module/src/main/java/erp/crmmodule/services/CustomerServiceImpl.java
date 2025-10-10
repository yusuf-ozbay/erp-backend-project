package erp.crmmodule.services;

import erp.commonmodule.exception.BusinessException;
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
    private final BonusTransactionService bonusTransactionService;

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
        CustomerEntity updated = addBonus(customerId, request.getAmount(), request.getDescription());
        return customerMapper.toDto(updated);
    }

    @Override
    public List<BonusTransactionDto> listBonusTransactions(Long customerId) {
        return bonusTransactionService.listTransactions(customerId);
    }


    @Override
    @Transactional
    public void applyBonusChange(Long customerId, BigDecimal delta, String description) {
        applyDelta(customerId, delta, description);
    }



    @Override
    @Transactional
    public CustomerEntity addBonus(Long customerId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(ErrorCode.BONUS_NEGATIVE_OR_ZERO);
        }

        CustomerEntity customer = customerDao.findById(customerId)
                .orElseThrow(() -> new erp.commonmodule.exception.ResourceNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));

        return applyDeltaInternal(customer, amount,
                "Bonus eklendi: " + (description == null ? "" : description));
    }



    @Override
    @Transactional
    public void applyDelta(Long customerId, BigDecimal delta, String description) {

        CustomerEntity customer = customerDao.findById(customerId)
                .orElseThrow(() -> new erp.commonmodule.exception.ResourceNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));

        if (delta.signum() < 0 && customer.getBonus().compareTo(delta.abs()) < 0) {
            throw new BusinessException(ErrorCode.INVOICE_BONUS_INSUFFICIENT);
        }

        applyDeltaInternal(customer, delta, (description == null ? "" : description));
    }



    private CustomerEntity applyDeltaInternal(CustomerEntity customer, BigDecimal delta, String description) {
        BigDecimal updated = customer.getBonus().add(delta);

        if (updated.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.BONUS_BALANCE_NEGATIVE);
        }
        customer.setBonus(updated);
        bonusTransactionService.save(customer, delta, description);

        return customer;
    }





}
