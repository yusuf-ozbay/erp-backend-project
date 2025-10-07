package erp.commonmodule.exception;

/**
 * İş kuralı ihlallerinde kullanılır.
 * Örn: Yetersiz bonus, bakiye sıfır altı vb.
 */
public class BusinessException extends BaseException {
    public BusinessException(ErrorCode error) { super(error); }
    public BusinessException(ErrorCode error, String msg) { super(error, msg); }
}
