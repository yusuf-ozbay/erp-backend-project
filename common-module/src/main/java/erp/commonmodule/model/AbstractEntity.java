package erp.commonmodule.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;  //Optimistic locking (eşzamanlı güncelleme çakışmalarına karşı)

    @Column(name="created_at", nullable=false, updatable=false)
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable=false, updatable=false)
    private Instant updatedAt;

}
