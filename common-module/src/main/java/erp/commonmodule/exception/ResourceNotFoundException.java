package erp.commonmodule.exception;

/**
 * İstenen kaynak bulunamadığında kullanılır.
 * Örn: customerId hatalı.
 */
public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(ErrorCode error) { super(error); }
    public ResourceNotFoundException(ErrorCode error, String msg) { super(error, msg); }
}
