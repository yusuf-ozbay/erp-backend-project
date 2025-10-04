package erp.crmmodule.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CustomerDto {
    private Long id;
    private String name;
    private String email;
    private BigDecimal bonus;
}
