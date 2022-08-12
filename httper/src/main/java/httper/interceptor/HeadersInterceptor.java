package httper.interceptor;

import java.io.IOException;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeadersInterceptor implements Interceptor {

    private final Map<String, Object> headers;

    public HeadersInterceptor(Map<String, Object> headers) {
        this.headers = headers;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if (headers == null) {
            return chain.proceed(builder.build());
        }
        try {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chain.proceed(builder.build());

    }
}