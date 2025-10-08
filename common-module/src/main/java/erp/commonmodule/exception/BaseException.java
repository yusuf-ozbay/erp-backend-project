package erp.commonmodule.exception;

import lombok.Getter;

/**
 * Tüm özel exception'ların temel sınıfı.
 * - ErrorCode taşır (hem HTTP status hem business code'u içerir)
 * - Mesaj; ErrorCode default'u ya da override edilen özel mesaj olabilir.
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final ErrorCode error;

    protected BaseException(ErrorCode error) {
        super(error.getDefaultMessage());
        this.error = error;
    }


    /** ApiResponse.status alanına gidecek business kod */
    public int getCode() { return error.getCode(); }
}
