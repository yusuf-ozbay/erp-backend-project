package erp.invoicemodule.services;

import erp.invoicemodule.dto.InvoiceDto;
import erp.invoicemodule.dto.InvoiceRequestDto;


public interface InvoiceService {
    InvoiceDto createInvoice(InvoiceRequestDto request);
}

