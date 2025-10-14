package erp.crmmodule.dto;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class BonusRequestDto {
    private BigDecimal amount;
    private String description;
}


