package erp.crmmodule.mapper;

import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.models.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerDto toDto(CustomerEntity entity);
    CustomerEntity toEntity(CustomerDto dto);
}
