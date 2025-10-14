package erp.crmmodule.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;


@Data
public class BonusDto {
    private Long id;
    private BigDecimal amount;
    private String description;
    private Instant createdAt;
}


