package erp.crmmodule.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class BonusTransactionDto {
    private Long id;
    private BigDecimal amount;
    private String description;
    private Instant createdAt;
}



/**
 * BonusTransactionDto
 * - Dokümanda: "BonusTransaction: Müşterinin bonus değişimlerini kaydeder, tarih, miktar, açıklama."
 * - Yani sadece bu alanlar DTO'da olmalı, fazlası yok.
 */
