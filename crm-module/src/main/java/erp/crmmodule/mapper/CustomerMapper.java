package erp.crmmodule.mapper;

import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.models.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * MapStruct Mapper
 * - Dokümanda: "Entity ↔ DTO dönüşümleri için Mapper kullanılacak (MapStruct)."
 * - Bu arayüz sayesinde CustomerEntity <-> CustomerDto dönüşümleri otomatik üretilir.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDto toDto(CustomerEntity entity);
    CustomerEntity toEntity(CustomerDto dto);

    List<CustomerDto> toDtoList(List<CustomerEntity> entities);
}
