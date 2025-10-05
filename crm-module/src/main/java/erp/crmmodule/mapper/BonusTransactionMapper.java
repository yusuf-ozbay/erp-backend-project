package erp.crmmodule.mapper;

import erp.crmmodule.dto.BonusTransactionDto;
import erp.crmmodule.models.BonusTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * BonusTransactionMapper
 * - BonusTransactionEntity <-> BonusTransactionDto dönüşümü
 * - Dokümanda "BonusTransaction: tarih, miktar, açıklama" belirtildiği için sadece bunlar DTO'ya yansır.
 */
@Mapper(componentModel = "spring")
public interface BonusTransactionMapper {

    BonusTransactionDto toDto(BonusTransactionEntity entity);
    List<BonusTransactionDto> toDtoList(List<BonusTransactionEntity> entities);
}
