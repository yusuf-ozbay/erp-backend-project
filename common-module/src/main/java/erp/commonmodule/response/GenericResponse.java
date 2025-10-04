package erp.commonmodule.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericResponse<T> {
    private String outcome_type; // success / error
    private int status;          // 0=ok, 1=fail
    private Object query;
    private T data;
    private Object uimessage;
    private Object iomessage;

    public static <T> GenericResponse<T> success(T data) {
        return new GenericResponse<>("success", 0, null, data, null, null);
    }

    public static <T> GenericResponse<T> error(String message) {
        return new GenericResponse<>("error", 1, null, null, message, null);
    }
}
