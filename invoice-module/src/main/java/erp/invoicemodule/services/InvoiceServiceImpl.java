package erp.invoicemodule.services;

import erp.commonmodule.exception.BusinessException;
import erp.commonmodule.exception.ErrorCode;
import erp.commonmodule.exception.ResourceNotFoundException;
import erp.commonmodule.exception.ValidationException;
import erp.crmmodule.dto.CustomerDto;
import erp.crmmodule.models.CustomerEntity;
import erp.crmmodule.services.CustomerService; // 👈 DAO yerine CRM servisi
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

    private final InvoiceDao invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceLineMapper invoiceLineMapper;

    // 👇 Yeni: DAO yerine CRM servis katmanı
    private final CustomerService customerService;


    @PersistenceContext
    private EntityManager em;  // 👈 yalın referans almak için

    /**
     * createInvoice:
     * - Fatura kaydı + satırları
     * - Tipine göre bonusu CRM servisinden düş/ekle (delta)
     * - Audit ve bakiye kuralı CRM’de uygulanır (tek otorite)
     * - amount >= 0, invalid type kontrolü burada
     */
    @Override
    @Transactional
    public InvoiceDto createInvoice(InvoiceRequestDto request) {
        // 0) amount negatif olamaz
        if (request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(ErrorCode.INVOICE_NEGATIVE_AMOUNT);
        }

        // 1) Müşteri var mı? (CRM servisinden doğruluyoruz)
        CustomerDto customer = customerService.getById(request.getCustomerId()); // yoksa 404 fırlar

        // 2) Fatura tipi güvenli parse
        InvoiceType type;
        try {
            type = InvoiceType.valueOf(request.getType());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVOICE_INVALID_TYPE);
        }

        // 3) Fatura entity
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setType(type);
        invoice.setTotalAmount(request.getAmount());

        // 🔴 KRİTİK: customer_id'yi set et
        CustomerEntity customerRef = em.getReference(CustomerEntity.class, request.getCustomerId());
        invoice.setCustomer(customerRef);


        // customerId DTO’dan maplenecek (InvoiceMapper ile), burada sadece satırları setliyoruz
        List<InvoiceLineEntity> lineEntities = invoiceLineMapper.toEntityList(request.getLines());
        lineEntities.forEach(line -> line.setInvoice(invoice));
        invoice.setLines(lineEntities);

        // 4) Bonus delta’yı CRM uygulasın + audit’i CRM oluştursun
        //    Satış → delta NEGATİF, İade → delta POZİTİF (doküman senaryosu)
        BigDecimal delta = switch (type) {
            case RETAIL_SALE, WHOLESALE_SALE -> request.getAmount().negate();
            case RETAIL_RETURN, WHOLESALE_RETURN -> request.getAmount();
        };
        String desc = (delta.signum() < 0 ? "Bonus harcandı" : "Bonus iade edildi") + " (fatura: " + type + ")";
        customerService.applyBonusChange(request.getCustomerId(), delta, desc);

        // 5) Faturayı kaydet
        //    Not: Customer relation’u mapper ile setlenir (customerId üzerinden)
        //    DTO → Entity map’inde customerId -> customer.id kuralı InvoiceMapper’da tanımlı.
        InvoiceEntity saved = invoiceRepository.save(invoice);

        // 6) DTO dön
        return invoiceMapper.toDto(saved);
    }
}
