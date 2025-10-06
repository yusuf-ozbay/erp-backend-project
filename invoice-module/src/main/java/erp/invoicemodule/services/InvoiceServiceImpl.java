package erp.invoicemodule.services;

import erp.commonmodule.exception.BusinessException;

import erp.crmmodule.dao.BonusTransactionDao;
import erp.crmmodule.dao.CustomerDao;
import erp.crmmodule.models.BonusTransactionEntity;
import erp.crmmodule.models.CustomerEntity;

import erp.invoicemodule.dao.InvoiceDao;
import erp.invoicemodule.dto.InvoiceDto;
import erp.invoicemodule.dto.InvoiceRequestDto;
import erp.invoicemodule.mapper.InvoiceLineMapper;
import erp.invoicemodule.mapper.InvoiceMapper;
import erp.invoicemodule.models.InvoiceEntity;
import erp.invoicemodule.models.InvoiceLineEntity;
import erp.invoicemodule.models.InvoiceType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceDao invoiceRepository;
    private final CustomerDao customerRepository;
    private final BonusTransactionDao bonusTransactionRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceLineMapper invoiceLineMapper;

    /**
     * createInvoice:
     * - Fatura kaydeder
     * - Fatura tipine göre bonusu düşer veya ekler
     * - BonusTransaction (audit) oluşturur
     * - Bonus negatif olamaz (kontrol)
     */
    @Override
    @Transactional
    public InvoiceDto createInvoice(InvoiceRequestDto request) {
        // 1️⃣ Müşteri bulunur
        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException(2001, "Müşteri bulunamadı"));

        // 2️⃣ Fatura entity oluşturulur
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setCustomer(customer);
        invoice.setType(InvoiceType.valueOf(request.getType()));
        invoice.setTotalAmount(request.getAmount());

        // 3️⃣ Fatura satırlarını map et (InvoiceLineDto → InvoiceLineEntity)
        List<InvoiceLineEntity> lineEntities = invoiceLineMapper.toEntityList(request.getLines());
        lineEntities.forEach(line -> line.setInvoice(invoice));
        invoice.setLines(lineEntities);

        // 4️⃣ Geçersiz bonus tutarı kontrolü (negatif amount)
        if (request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(2003, "Negatif bonus tutarı ile işlem yapılamaz");
        }

        // 5️⃣ Bonus iş mantığı
        BigDecimal amount = request.getAmount();
        String txDescription; // BonusTransaction için açıklama

        switch (invoice.getType()) {
            case RETAIL_SALE:
            case WHOLESALE_SALE:
                // Satış → bonus harcanır
                if (customer.getBonus().compareTo(amount) < 0) {
                    throw new BusinessException(2002, "Yetersiz bonus bakiyesi");
                }
                customer.setBonus(customer.getBonus().subtract(amount));
                txDescription = "Bonus harcandı (fatura: " + invoice.getType() + ")";
                break;

            case RETAIL_RETURN:
            case WHOLESALE_RETURN:
                // İade → bonus geri eklenir
                customer.setBonus(customer.getBonus().add(amount));
                txDescription = "Bonus iade edildi (fatura: " + invoice.getType() + ")";
                break;

            default:
                throw new BusinessException(2004, "Geçersiz fatura tipi");
        }

        // 6️⃣ Bonus negatif olamaz (güncel bakiye kontrolü)
        if (customer.getBonus().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(2005, "Bonus bakiyesi sıfırın altına düşemez");
        }

        // 7️⃣ BonusTransaction (audit) oluştur
        BonusTransactionEntity tx = new BonusTransactionEntity();
        tx.setCustomer(customer);
        tx.setAmount(amount);
        tx.setDescription(txDescription);
        bonusTransactionRepository.save(tx);

        // 8️⃣ Customer & Invoice kaydet
        customerRepository.save(customer);
        InvoiceEntity savedInvoice = invoiceRepository.save(invoice);

        // 9️⃣ DTO dön
        return invoiceMapper.toDto(savedInvoice);    }
}
