package erp.crmmodule.mapper;

import erp.crmmodule.dto.BonusDto;
import erp.crmmodule.models.BonusEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BonusMapper {

    BonusDto toDto(BonusEntity entity);
    List<BonusDto> toDtoList(List<BonusEntity> entities);
}



/**
 * BonusMapper
 * - BonusEntity <-> BonusDto dönüşümünü yapar.
 */
