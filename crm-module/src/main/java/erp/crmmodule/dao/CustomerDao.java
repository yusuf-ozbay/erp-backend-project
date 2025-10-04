package erp.crmmodule.dao;

import erp.crmmodule.models.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


@Repository
public interface CustomerDao extends JpaRepository<CustomerEntity, Long> {

    boolean existsByEmail(String email);

    List<CustomerEntity> findByBonusBalanceBetween(BigDecimal min, BigDecimal max);
}
