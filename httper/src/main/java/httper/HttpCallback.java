package httper;

public interface HttpCallback<T> {
    void onResult(HttpResponse<T> httpResponse);
}

// public class ApiResult<T> {
//     public int code;
//     public String msg;
//     public T data;
// }

// public interface ApiResultCallback<T> extends HttpCallback<ApiResult<T>> {
//
// }
