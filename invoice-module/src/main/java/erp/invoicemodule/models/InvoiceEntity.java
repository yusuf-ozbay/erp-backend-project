package erp.invoicemodule.models;

import erp.commonmodule.model.AbstractEntity;
import erp.crmmodule.models.CustomerEntity;
import erp.invoicemodule.enums.InvoiceType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * InvoiceEntity
 * - Dokümandaki tanıma uygun: Customer bağlantılı, type alanı var, line tabloları var.
 */
@Data
@Entity
@Table(name = "invoices")
public class InvoiceEntity extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceType type;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    // Line tablolarla ilişki (1-N)
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLineEntity> lines = new ArrayList<>();
}
