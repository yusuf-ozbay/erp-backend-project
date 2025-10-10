package erp.invoicemodule.services;

import erp.commonmodule.exception.BusinessException;
import erp.commonmodule.exception.ErrorCode;
import erp.commonmodule.exception.ValidationException;
import erp.crmmodule.models.CustomerEntity;
import erp.crmmodule.services.CustomerService;
import erp.invoicemodule.dao.InvoiceDao;
import erp.invoicemodule.dto.InvoiceDto;
import erp.invoicemodule.dto.InvoiceRequestDto;
import erp.invoicemodule.enums.InvoiceType;
import erp.invoicemodule.mapper.InvoiceLineMapper;
import erp.invoicemodule.mapper.InvoiceMapper;
import erp.invoicemodule.models.InvoiceEntity;
import erp.invoicemodule.models.InvoiceLineEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceDao invoiceDao;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceLineMapper invoiceLineMapper;

    private final CustomerService customerService;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public InvoiceDto createInvoice(InvoiceRequestDto request) {
        // 0) amount negatif olamaz
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(ErrorCode.INVOICE_NEGATIVE_AMOUNT);
        }

        final InvoiceType type;
        try {
            type = InvoiceType.valueOf(request.getType());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVOICE_INVALID_TYPE);
        }

        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setType(type);
        invoice.setTotalAmount(request.getAmount());

        CustomerEntity customerRef = em.getReference(CustomerEntity.class, request.getCustomerId());
        invoice.setCustomer(customerRef);

        List<InvoiceLineEntity> lineEntities = invoiceLineMapper.toEntityList(request.getLines());
        lineEntities.forEach(line -> line.setInvoice(invoice));
        invoice.setLines(lineEntities);

        BigDecimal delta = switch (type) {
            case RETAIL_SALE, WHOLESALE_SALE -> request.getAmount().negate();
            case RETAIL_RETURN, WHOLESALE_RETURN -> request.getAmount();
        };
        String desc = (delta.signum() < 0 ? "Bonus harcandı" : "Bonus iade edildi") + " (fatura: " + type + ")";
        customerService.applyBonusChange(request.getCustomerId(), delta, desc);

        InvoiceEntity saved = invoiceDao.save(invoice);

        return invoiceMapper.toDto(saved);
    }
}
