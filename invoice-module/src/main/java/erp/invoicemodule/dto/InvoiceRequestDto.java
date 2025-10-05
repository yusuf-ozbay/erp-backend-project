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



/**
 * InvoiceRequestDto
 * - Dokümandaki örneğe birebir uyuyor:
 *   {
 *     "customerId": 1,
 *     "type": "RETAIL_SALE",
 *     "amount": 200,
 *     "lines": [
 *       {"productId": 1, "quantity": 2, "price": 100}
 *     ]
 *   }
 * - Burada 'amount' = toplam tutar.
 */