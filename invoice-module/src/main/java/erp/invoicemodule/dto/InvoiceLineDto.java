package erp.invoicemodule.dto;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class InvoiceLineDto {
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
}
