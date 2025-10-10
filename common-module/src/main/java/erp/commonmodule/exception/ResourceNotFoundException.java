package erp.commonmodule.exception;

/**
 * İstenen kaynak bulunamadığında kullanılır.
 * Örn: customerId hatalı. GlobalExceptionHandler ile 404 döner.
 */
public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(ErrorCode error) { super(error); }
}
