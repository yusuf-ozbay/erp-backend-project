package erp.crmmodule.services;

import erp.commonmodule.exception.ErrorCode;                                 // Hata kodları
import erp.commonmodule.exception.ResourceNotFoundException;                // 404
import erp.commonmodule.exception.ValidationException;                       // giriş doğrulama
import erp.crmmodule.dao.CustomerDao;                                        // sadece müşteri repo
import erp.crmmodule.dto.BonusRequestDto;                                    // bonus ekleme isteği
import erp.crmmodule.dto.BonusTransactionDto;                                // hareket DTO
import erp.crmmodule.dto.CustomerDto;                                        // müşteri DTO
import erp.crmmodule.mapper.CustomerMapper;                                  // entity<->dto
import erp.crmmodule.models.CustomerEntity;                                  // müşteri entity
import jakarta.persistence.criteria.Predicate;                                // spesifikasyon için
import org.springframework.data.jpa.domain.Specification;                    // dinamik filtre
import jakarta.transaction.Transactional;                                     // tx
import lombok.RequiredArgsConstructor;                                        // ctor inj
import org.springframework.stereotype.Service;                                // service

import java.math.BigDecimal;                                                  // para tipi
import java.util.ArrayList;                                                   // liste
import java.util.List;                                                        // liste

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDao customerRepository;                              // sadece Customer repo
    private final CustomerMapper customerMapper;                               // MapStruct
    private final BonusLedgerService bonusLedgerService;                       //  Bonus işlerini delege ettiğimiz servis

    @Override
    public CustomerDto createCustomer(CustomerDto customerDto) {
        // Email uniq kontrolü
        if (customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new ValidationException(ErrorCode.CUSTOMER_EMAIL_EXISTS);
        }
        // DTO -> Entity
        CustomerEntity entity = customerMapper.toEntity(customerDto);
        entity.setBonus(BigDecimal.ZERO);                                      // başlangıç bonusu 0
        CustomerEntity saved = customerRepository.save(entity);                // kaydet
        return customerMapper.toDto(saved);                                    // Entity -> DTO
    }

    @Override
    public List<CustomerDto> listCustomers(BigDecimal minBonus, BigDecimal maxBonus) {
        // Dinamik filtre (Specification)
        Specification<CustomerEntity> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (minBonus != null) preds.add(cb.greaterThanOrEqualTo(root.get("bonus"), minBonus));
            if (maxBonus != null) preds.add(cb.lessThanOrEqualTo(root.get("bonus"), maxBonus));
            return cb.and(preds.toArray(new Predicate[0]));
        };

        // Stream + mapper (mapStruct)
        return customerRepository.findAll(spec)
                .stream()
                .map(customerMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CustomerDto addBonus(Long customerId, BonusRequestDto request) {
        // Tüm doğrulama + delta + hareket = BonusLedgerService
        CustomerEntity updated = bonusLedgerService.addBonus(customerId,request.getAmount(),request.getDescription());
        return customerMapper.toDto(updated);                                   // Güncel bakiyeyle DTO dön
    }

    @Override
    public List<BonusTransactionDto> listBonusTransactions(Long customerId) {
        // Ledger servisinden oku (tek otorite)
        return bonusLedgerService.listTransactions(customerId);
    }


    @Override
    @Transactional
    public void applyBonusChange(Long customerId, BigDecimal delta, String description) {
        // Satış/iade delta uygulamasını ledger’a delege et
        bonusLedgerService.applyDelta(customerId, delta, description);
    }
}
