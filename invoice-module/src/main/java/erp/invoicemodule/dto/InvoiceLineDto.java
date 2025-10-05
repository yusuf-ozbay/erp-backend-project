package erp.invoicemodule.dto;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class InvoiceLineDto {
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
}


/**
 * InvoiceLineDto
 * - Dokümanda "line tablolarla yönetilmeli" denildiği için
 *   fatura satırları için ayrı DTO tanımlıyoruz.
 * - Entity’deki InvoiceLineEntity’nin dışa açılan versiyonu.
 */