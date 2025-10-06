package erp.crmmodule.services;

import erp.commonmodule.exception.BusinessException;
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
     * - Dokümanda: "POST /api/customers"
     * - Email benzersiz olmalı.
     */
    @Override
    public CustomerDto createCustomer(CustomerDto customerDto) {
        if (customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new BusinessException(1001, "Email zaten kayıtlı");
        }
        CustomerEntity entity = customerMapper.toEntity(customerDto);
        entity.setBonus(BigDecimal.ZERO); // başlangıç bonusu sıfır
        CustomerEntity saved = customerRepository.save(entity);
        return customerMapper.toDto(saved);
    }

    /**
     * Müşteri listesini getirir
     * - Dokümanda: "GET /api/customers"
     * - minBonus / maxBonus filtreleri opsiyonel olacak.
     */
    @Override
    public List<CustomerDto> listCustomers(BigDecimal minBonus, BigDecimal maxBonus) {
        if (minBonus != null && maxBonus != null) {
            return customerMapper.toDtoList(customerRepository.findByBonusBetween(minBonus, maxBonus));
        }
        return customerMapper.toDtoList(customerRepository.findAll());
    }



//    @Override
//    public List<CustomerDto> listCustomers(BigDecimal minBonus, BigDecimal maxBonus){
//
//        // Specification<CustomerEntity> objesi oluşturulur.
//        Specification<CustomerEntity> spec = (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // 1. Minimum Bonus Filtresi (minBonus parametresi varsa uygulanır)
//            if (minBonus != null) {
//                // CustomerEntity'deki "bonusBalance" alanının, minBonus değerinden BÜYÜK veya EŞİT olmasını şart koşar (>=)
//                predicates.add(cb.greaterThanOrEqualTo(root.get("bonusBalance"), minBonus));
//            }
//
//            // 2. Maksimum Bonus Filtresi (maxBonus parametresi varsa uygulanır)
//            if (maxBonus != null) {
//                // CustomerEntity'deki "bonusBalance" alanının, maxBonus değerinden KÜÇÜK veya EŞİT olmasını şart koşar (<=)
//                predicates.add(cb.lessThanOrEqualTo(root.get("bonusBalance"), maxBonus));
//            }
//
//            // Oluşturulan tüm şartları (Predicate) mantıksal AND ile birleştirir.
//            // Eğer Predicates listesi boşsa, cb.and() tüm kayıtları döndürür.
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
//
//        // Repository'deki findAll(Specification) metodu çağrılır.
//        List<CustomerEntity> customers = customerRepository.findAll(spec);
//
//        // Sonucu DTO listesine dönüştürerek döndür.
//        return customerMapper.toDtoList(customers);
//    }




    /**
     * Müşteriye bonus ekler
     * - Dokümanda: "POST /api/customers/{id}/bonus"
     * - Bonus tablosuna kayıt açılır
     * - Customer bonus güncellenir
     * - BonusTransaction kaydı oluşturulur
     * - Negatif veya sıfır bonus kabul edilmez (doküman gereği)
     */
    @Override
    @Transactional
    public CustomerDto addBonus(Long customerId, BonusRequestDto request) {
        // 1️⃣ Müşteri kontrolü
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(1002, "Müşteri bulunamadı"));

        // 2️⃣ Negatif veya sıfır bonus kontrolü
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(1004, "Negatif veya sıfır bonus eklenemez");
        }

        // 3️⃣ Bonus kaydı oluştur (Bonus tablosuna)
        BonusEntity bonus = new BonusEntity();
        bonus.setCustomer(customer);
        bonus.setAmount(request.getAmount());
        bonus.setDescription(request.getDescription());
        bonusRepository.save(bonus);

        // 4️⃣ Customer bonus bakiyesini güncelle
        BigDecimal updatedBalance = customer.getBonus().add(request.getAmount());

        // 💡 (Ek güvenlik) Negatif bonus oluşmaması için koruma
        if (updatedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(1005, "Bonus bakiyesi sıfırın altına düşemez");
        }

        customer.setBonus(updatedBalance);
        customerRepository.save(customer);

        // 5️⃣ BonusTransaction kaydı oluştur (audit log)
        BonusTransactionEntity tx = new BonusTransactionEntity();
        tx.setCustomer(customer);
        tx.setAmount(request.getAmount());
        tx.setDescription("Bonus eklendi: " + request.getDescription());
        bonusTransactionRepository.save(tx);

        // 6️⃣ DTO dön
        return customerMapper.toDto(customer);
    }

    /**
     * Müşteriye ait bonus transaction listesini getirir
     * - Dokümanda: "GET /api/customers/{id}/bonus-transactions"
     */
    @Override
    public List<BonusTransactionDto> listBonusTransactions(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException(1003, "Müşteri bulunamadı");
        }
        return bonusTransactionMapper.toDtoList(
                bonusTransactionRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId)
        );
    }
}
