package erp.crmmodule.controllers;

import erp.commonmodule.response.GenericResponse;
import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public GenericResponse<CustomerDto> createCustomer(@RequestBody CustomerDto dto) {
        return GenericResponse.success(customerService.createCustomer(dto));
    }

    @GetMapping
    public GenericResponse<List<CustomerDto>> getAllCustomers() {
        return GenericResponse.success(customerService.getAllCustomers());
    }
}
