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

    // --- Versiyon alanı hemen ID'den sonra kalabilir ---
    @Version
    private Long version;

    // --- Geliştiricinin Entity'nin kendine ait alanları (name, email, vb.) buraya gelir ---

    // --- Denetim (Audit) Alanları EN SONA taşındı ---

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        // Instant.now() kullanmaya devam ediyoruz
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
