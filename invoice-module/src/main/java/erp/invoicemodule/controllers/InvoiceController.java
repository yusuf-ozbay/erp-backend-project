package erp.invoicemodule.controllers;

import erp.commonmodule.response.ApiResponse;
import erp.invoicemodule.dto.InvoiceDto;
import erp.invoicemodule.dto.InvoiceRequestDto;
import erp.invoicemodule.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceDto>> createInvoice(@RequestBody InvoiceRequestDto request) {
        InvoiceDto created = invoiceService.createInvoice(request);
        return ResponseEntity.ok(ApiResponse.success(created));
    }
}
