package erp.crmmodule.models;

import erp.commonmodule.model.AbstractEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "customers")
public class CustomerEntity extends AbstractEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "bonus_balance", nullable = false)
    private BigDecimal bonusBalance = BigDecimal.ZERO;
}
