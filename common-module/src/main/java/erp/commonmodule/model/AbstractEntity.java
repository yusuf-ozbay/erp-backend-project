package erp.commonmodule.model;


import jakarta.persistence.*;
import lombok.Data;

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
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
