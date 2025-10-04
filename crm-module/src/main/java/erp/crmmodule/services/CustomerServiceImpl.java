package erp.crmmodule.services;

import erp.crmmodule.dao.CustomerDao;
import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.mapper.CustomerMapper;
import erp.crmmodule.models.CustomerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDao customerDao;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerDto createCustomer(CustomerDto dto) {
        CustomerEntity entity = customerMapper.toEntity(dto);
        entity = customerDao.save(entity);
        return customerMapper.toDto(entity);
    }

    @Override
    public List<CustomerDto> getAllCustomers() {
        return customerDao.findAll()
                .stream()
                .map(customerMapper::toDto)
                .toList();
    }
}
