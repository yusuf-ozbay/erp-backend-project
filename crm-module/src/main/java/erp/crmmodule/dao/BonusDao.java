package erp.crmmodule.dao;

import erp.crmmodule.models.BonusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BonusDao extends JpaRepository<BonusEntity, Long> {


}
