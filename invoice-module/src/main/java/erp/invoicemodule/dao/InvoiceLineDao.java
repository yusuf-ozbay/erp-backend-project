package erp.invoicemodule.dao;

import erp.invoicemodule.models.InvoiceLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceLineDao extends JpaRepository<InvoiceLineEntity, Long> {
}
