package erp.commonmodule.exception;

/**
 * Kullanıcıdan gelen hatalı girdi/validasyon sorunlarında kullanılır.
 * Örn: amount <= 0, zorunlu alan boş, format hatası vb.
 */
public class ValidationException extends BaseException {
    public ValidationException(ErrorCode error) { super(error); }
}
