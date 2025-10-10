package erp.crmmodule.dao;

import erp.crmmodule.models.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


@Repository
public interface CustomerDao extends JpaRepository<CustomerEntity, Long>, JpaSpecificationExecutor<CustomerEntity> {

    boolean existsByEmail(String email);

    List<CustomerEntity> findByBonusBetween(BigDecimal min, BigDecimal max);


}
