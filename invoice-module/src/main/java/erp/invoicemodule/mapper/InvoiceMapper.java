package erp.invoicemodule.mapper;

import erp.invoicemodule.dto.InvoiceDto;
import erp.invoicemodule.models.InvoiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper(componentModel = "spring", uses = {InvoiceLineMapper.class})
public interface InvoiceMapper {

    // Entity -> DTO
    @Mapping(source = "customer.id", target = "customerId")
    InvoiceDto toDto(InvoiceEntity entity);


}


/**
 * InvoiceMapper
 * - InvoiceEntity <-> InvoiceDto dönüşümü
 * - CustomerEntity -> sadece customerId olarak DTO'ya mapleniyor.
 */