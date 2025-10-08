package erp.commonmodule.exception;

/**
 * İstenen kaynak bulunamadığında kullanılır.
 * Örn: customerId hatalı.
 */
public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(ErrorCode error) { super(error); }
}
