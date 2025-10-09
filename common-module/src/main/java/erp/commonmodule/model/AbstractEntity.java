package erp.commonmodule.model;


import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * Tüm Entity'ler için temel alanları (id, version, audit) sağlayan soyut sınıftır.
 * Kolonların veritabanında daha temiz (sağda) görünmesi için
 * audit alanları (createdAt/updatedAt) en sona taşınmıştır.
 */
@Data
@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name="created_at", nullable=false, updatable=false)
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable=false, updatable=false)
    private Instant updatedAt;

}
