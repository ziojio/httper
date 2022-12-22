package httper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Response;

public class HttpResponse<T> implements Serializable {

    public int code;
    public String message;
    public Map<String, String> header;

    public T data;

    public Error error;

    public static <T> HttpResponse<T> error(int code, String errorMsg) {
        HttpResponse<T> resp = new HttpResponse<>();
        resp.error = new Error(code, errorMsg);
        return resp;
    }

    public static <T> HttpResponse<T> processResponse(Response response) {
        HttpResponse<T> resp = new HttpResponse<>();
        Headers headers = response.headers();
        if (headers.size() > 0) {
            resp.header = new HashMap<>();
            for (String key : headers.names()) {
                resp.header.put(key, headers.get(key));
            }
        }
        resp.code = response.code();
        resp.message = response.message();
        if (!response.isSuccessful()) {
            resp.error = new HttpResponse.Error(resp.code, resp.message);
        }
        return resp;
    }

    public boolean isSuccess() {
        return error == null;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", header=" + header +
                ", data=" + data +
                ", error=" + error +
                '}';
    }

    public static class Error implements Serializable {
        public int code;
        public String msg;

        public Error(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "Error{" +
                    "code='" + code + '\'' +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }

}
