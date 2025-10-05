package erp.invoicemodule.dao;

import erp.invoicemodule.models.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceDao extends JpaRepository<InvoiceEntity, Long> {
}
