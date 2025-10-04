package erp.crmmodule.dao;

import erp.crmmodule.models.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CustomerDao extends JpaRepository<CustomerEntity, Long> {
}
