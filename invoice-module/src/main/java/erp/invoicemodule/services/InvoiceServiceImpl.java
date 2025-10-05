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
     * Fatura oluşturma
     * - Dokümanda: "Fatura kesilmesi: Satış → bonus harcama, İade → bonus geri alma"
     * - İş mantığı:
     *   1) Müşteri bulunur
     *   2) Fatura + satırları kaydedilir
     *   3) InvoiceType'a göre bonus güncellenir
     *   4) BonusTransaction kaydedilir
     */
    @Override
    @Transactional
    public InvoiceDto createInvoice(InvoiceRequestDto request) {
        // 1) Müşteri bul
        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException(2001, "Müşteri bulunamadı"));

        // 2) Fatura entity oluştur
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setCustomer(customer);
        invoice.setType(InvoiceType.valueOf(request.getType())); // Enum kontrolü
        invoice.setTotalAmount(request.getAmount());

        // 3) Fatura satırlarını map et
        List<InvoiceLineEntity> lineEntities = invoiceLineMapper.toEntityList(request.getLines());
        lineEntities.forEach(line -> line.setInvoice(invoice));
        invoice.setLines(lineEntities);

        // 4) Bonus iş mantığı
        BigDecimal amount = request.getAmount();
        switch (invoice.getType()) {
            case RETAIL_SALE:
            case WHOLESALE_SALE:
                // Satış: bonus düşülür
                if (customer.getBonus().compareTo(amount) < 0) {
                    throw new BusinessException(2002, "Yetersiz bonus bakiyesi");
                }
                customer.setBonus(customer.getBonus().subtract(amount));
                break;

            case RETAIL_RETURN:
            case WHOLESALE_RETURN:
                // İade: bonus eklenir
                customer.setBonus(customer.getBonus().add(amount));
                break;
        }

        // 5) BonusTransaction kaydı
        BonusTransactionEntity tx = new BonusTransactionEntity();
        tx.setCustomer(customer);
        tx.setAmount(amount);
        tx.setDescription("Invoice işlem: " + invoice.getType());
        bonusTransactionRepository.save(tx);

        // 6) Persist et
        customerRepository.save(customer);
        InvoiceEntity savedInvoice = invoiceRepository.save(invoice);

        // 7) DTO döndür
        return invoiceMapper.toDto(savedInvoice);
    }
}
