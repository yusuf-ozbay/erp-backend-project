package erp.invoicemodule.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class InvoiceRequestDto {
    private Long customerId;
    private String type;
    private BigDecimal amount;
    private List<InvoiceLineDto> lines;
}

