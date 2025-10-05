package erp.invoicemodule.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class InvoiceDto {
    private Long id;
    private Long customerId;
    private String type; // InvoiceType enum değerleri string olarak gelecek
    private BigDecimal totalAmount;
    private List<InvoiceLineDto> lines;
}


/**
 * InvoiceDto
 * - Dokümanda: "Invoice (Fatura): Customer bağlantılı, InvoiceType, line tablolar."
 * - Burada Customer sadece id ile temsil edilir (entity sızdırmıyoruz).
 * - InvoiceLineDto ile satırlar gösterilir.
 */