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



/**
 * DTO = Data Transfer Object
 * - Dokümanda beklenti: Controller katmanında Entity değil DTO kullanılacak.
 * - Bu sınıf CustomerEntity'nin dış dünyaya yansıyan basit versiyonu.
 * - Kalan bonus (bonusBalance) doğrudan burada gösterilecek (dokümanda özellikle belirtilmiş).
 */