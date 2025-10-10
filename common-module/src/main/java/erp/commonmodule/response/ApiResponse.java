package erp.commonmodule.response;

import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiResponse<T> {
    private String outcome_type;
    private int status;
    private Map<String, Object> query;
    private T data;
    private List<String> uimessage;
    private List<String> iomessage;

    public static <T> ApiResponse<T> success(T data){
        return ApiResponse.<T>builder()
                .outcome_type("success")
                .status(200)
                .query(Collections.emptyMap())
                .data(data)
                .uimessage(Collections.emptyList())
                .iomessage(Collections.emptyList())
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message){
        return ApiResponse.<T>builder()
                .outcome_type("error")
                .status(status)
                .query(Collections.emptyMap())
                .data(null)
                .uimessage(List.of(message))
                .iomessage(Collections.emptyList())
                .build();
    }
}
