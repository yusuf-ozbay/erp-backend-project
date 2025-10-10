package erp.crmmodule.services;

import erp.crmmodule.dto.CustomerDto;

public interface CustomerLookupPort {
    CustomerDto getById(Long id);
}