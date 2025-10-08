package erp.commonmodule.exception;

import org.springframework.http.HttpStatus;

/**
 * Uygulama-özel (business) hata kodları.
 * - code  : API body'de (ApiResponse.status) görünür
 * - http  : HTTP status (ResponseEntity)
 * - msg   : Varsayılan kullanıcı mesajı
 *
 * Not: HTTP kodu ile business kodu farklı kavramlardır.
 * Örn. 400 Bad Request + status=2002 "Yetersiz bonus" gibi.
 */
public enum ErrorCode {

    // --- CRM / Customer (1000-1999)
    CUSTOMER_EMAIL_EXISTS(1001, HttpStatus.BAD_REQUEST, "Email zaten kayıtlı"),
    CUSTOMER_NOT_FOUND(1002, HttpStatus.NOT_FOUND, "Müşteri bulunamadı"),
    BONUS_NEGATIVE_OR_ZERO(1004, HttpStatus.UNPROCESSABLE_ENTITY, "Negatif veya sıfır bonus eklenemez"),
    BONUS_BALANCE_NEGATIVE(1005, HttpStatus.BAD_REQUEST, "Bonus bakiyesi sıfırın altına düşemez"),

    // --- Invoice (2000-2999)
    INVOICE_CUSTOMER_NOT_FOUND(2001, HttpStatus.NOT_FOUND, "Müşteri bulunamadı"),
    INVOICE_BONUS_INSUFFICIENT(2002, HttpStatus.BAD_REQUEST, "Yetersiz bonus bakiyesi"),
    INVOICE_NEGATIVE_AMOUNT(2003, HttpStatus.UNPROCESSABLE_ENTITY, "Negatif veya sıfır bonus tutarı ile işlem yapılamaz"),
    INVOICE_INVALID_TYPE(2004, HttpStatus.BAD_REQUEST, "Geçersiz fatura tipi"),
    INVOICE_BALANCE_BELOW_ZERO(2005, HttpStatus.BAD_REQUEST, "Bonus bakiyesi sıfırın altına düşemez"),

    // --- Genel
    VALIDATION_FAILED(4000, HttpStatus.UNPROCESSABLE_ENTITY, "Geçersiz veri"),
    DB_CONSTRAINT_VIOLATION(4090, HttpStatus.CONFLICT, "Veritabanı bütünlük hatası"),
    INTERNAL_ERROR(9000, HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata");

    private final int code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(int code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() { return code; }
    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getDefaultMessage() { return defaultMessage; }
}
