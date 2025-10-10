package erp.crmmodule.services;

import erp.commonmodule.exception.ErrorCode;
import erp.commonmodule.exception.ResourceNotFoundException;
import erp.crmmodule.dao.CustomerDao;
import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.mapper.CustomerMapper;
import erp.crmmodule.models.CustomerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * Sadece “müşteri var mı?” diye bakmak için küçük bir kapı (port).
 * Amaç: Bonus tarafı, müşteri servisine sıkı bağlanmasın; sadece okuma yapsın.
 * Böylece dairesel bağımlılık ve modüller arası gereksiz bağlılık oluşmaz.
 */

@Service
@RequiredArgsConstructor
public class CustomerQueryServiceImpl implements CustomerLookupPort {

    private final CustomerDao customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerDto getById(Long id) {
        CustomerEntity c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND));
        return customerMapper.toDto(c);
    }
}
