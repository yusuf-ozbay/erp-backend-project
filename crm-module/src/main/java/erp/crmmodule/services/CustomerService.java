package erp.crmmodule.services;

import erp.crmmodule.dto.CustomerDto;

import java.util.List;

public interface CustomerService {
    CustomerDto createCustomer(CustomerDto dto);
    List<CustomerDto> getAllCustomers();
}
