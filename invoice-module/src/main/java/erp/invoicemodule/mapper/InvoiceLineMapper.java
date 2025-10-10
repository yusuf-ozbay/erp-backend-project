package erp.invoicemodule.mapper;

import erp.invoicemodule.dto.InvoiceLineDto;
import erp.invoicemodule.models.InvoiceLineEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper(componentModel = "spring")
public interface InvoiceLineMapper {

    InvoiceLineDto toDto(InvoiceLineEntity entity);
    InvoiceLineEntity toEntity(InvoiceLineDto dto);

    List<InvoiceLineDto> toDtoList(List<InvoiceLineEntity> entities);
    List<InvoiceLineEntity> toEntityList(List<InvoiceLineDto> dtos);

}

