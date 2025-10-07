package erp.invoicemodule.services;

import erp.commonmodule.exception.*; // 👈 ErrorCode + exception tipleri
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
import erp.invoicemodule.enums.InvoiceType;
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
     * - Fatura kaydı + satırları
     * - Tipine göre bonusu düş/ekle
     * - Audit: BonusTransaction
     * - Kısıtlar: amount >= 0, bakiye < 0 olamaz
     */
    @Override
    @Transactional
    public InvoiceDto createInvoice(InvoiceRequestDto request) {
        // 1) Müşteri var mı? yoksa 404 + 2001
        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVOICE_CUSTOMER_NOT_FOUND));

        // 2) Fatura entity
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setCustomer(customer);
        invoice.setType(InvoiceType.valueOf(request.getType())); // enum doğrulaması
        invoice.setTotalAmount(request.getAmount());

        // 3) Satırları map et → invoice ilişkilendir
        List<InvoiceLineEntity> lineEntities = invoiceLineMapper.toEntityList(request.getLines());
        lineEntities.forEach(line -> line.setInvoice(invoice));
        invoice.setLines(lineEntities);

        // 4) amount negatif olamaz (doküman gereği)
        if (request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(ErrorCode.INVOICE_NEGATIVE_AMOUNT);
        }

        // 5) Bonus iş akışı
        BigDecimal amount = request.getAmount();
        String txDescription;

        switch (invoice.getType()) {
            case RETAIL_SALE, WHOLESALE_SALE -> {
                // Satış → bonus harcanır; bakiye yeterli olmalı
                if (customer.getBonus().compareTo(amount) < 0) {
                    throw new BusinessException(ErrorCode.INVOICE_BONUS_INSUFFICIENT);
                }
                customer.setBonus(customer.getBonus().subtract(amount));
                txDescription = "Bonus harcandı (fatura: " + invoice.getType() + ")";
            }
            case RETAIL_RETURN, WHOLESALE_RETURN -> {
                // İade → bonus eklenir
                customer.setBonus(customer.getBonus().add(amount));
                txDescription = "Bonus iade edildi (fatura: " + invoice.getType() + ")";
            }
            default -> throw new BusinessException(ErrorCode.INVOICE_INVALID_TYPE);
        }

        // 6) Bakiye asla < 0 olamaz (ek güvenlik)
        if (customer.getBonus().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.INVOICE_BALANCE_BELOW_ZERO);
        }

        // 7) Audit kaydı (BonusTransaction)
        BonusTransactionEntity tx = new BonusTransactionEntity();
        tx.setCustomer(customer);
        tx.setAmount(amount);
        tx.setDescription(txDescription);
        bonusTransactionRepository.save(tx);

        // 8) Kalıcı hale getir
        customerRepository.save(customer);
        InvoiceEntity savedInvoice = invoiceRepository.save(invoice);

        // 9) DTO dön
        return invoiceMapper.toDto(savedInvoice);
    }
}
