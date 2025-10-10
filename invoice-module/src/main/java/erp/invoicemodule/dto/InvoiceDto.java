package erp.invoicemodule.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class InvoiceDto {
    private Long id;
    private Long customerId;
    private String type;
    private BigDecimal totalAmount;
    private List<InvoiceLineDto> lines;
}
