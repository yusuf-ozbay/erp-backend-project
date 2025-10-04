package erp.crmmodule.dto;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class BonusRequestDto {
    private BigDecimal amount;
    private String description;
}



/**
 * BonusRequestDto
 * - API çağrısı ile bonus eklemek için kullanılacak request body.
 * - Dokümandaki örneğe birebir uyuyor: { "amount": 500, "description": "..." }
 */