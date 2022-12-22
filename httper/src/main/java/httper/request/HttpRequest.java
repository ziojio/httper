package httper.request;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import httper.HttpCallback;
import httper.HttpResponse;
import httper.Httper;
import httper.Parser;
import httper.RequestFilter;
import httper.util.TypeUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @see GetRequest
 * @see PostRequest
 * @see UploadRequest
 * @see DownloadRequest
 */
@SuppressWarnings("unchecked")
abstract class HttpRequest<T extends HttpRequest<?>> {
    static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain; charset=utf-8");
    static final MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");
    static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

    final boolean debug;
    final String baseUrl;
    final RequestFilter requestFilter;
    final Executor executor;
    final OkHttpClient httpClient;

    String url;
    String tag;
    long timeout;
    HashMap<String, String> headers = new HashMap<>();

    public HttpRequest(Httper httper) {
        debug = httper.isDebug();
        baseUrl = httper.getBaseUrl();
        executor = httper.getCallbackExecutor();
        requestFilter = httper.getRequestFilter();
        httpClient = httper.getHttpClient();

        if (httper.getHeaders() != null) {
            headers = new HashMap<>(httper.getHeaders());
        }
    }

    public T url(String url) {
        this.url = url;
        return (T) this;
    }

    public T timeout(long timeout, TimeUnit timeUnit) {
        this.timeout = timeUnit.toMillis(timeout);
        return (T) this;
    }

    public T headers(Map<String, String> headers) {
        if (this.headers != null) {
            this.headers.putAll(headers);
        } else {
            this.headers = new HashMap<>(headers);
        }
        return (T) this;
    }

    public T header(String key, String value) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(key, value);
        return (T) this;
    }

    public T tag(String tag) {
        this.tag = tag;
        return (T) this;
    }

    String generateUrl() {
        String httpUrl;
        if (url == null || url.isBlank()) {
            httpUrl = Objects.requireNonNull(baseUrl, "baseUrl is empty");
        } else if (url.startsWith("http")) {
            httpUrl = url;
        } else {
            httpUrl = Objects.requireNonNull(baseUrl, "baseUrl is empty") + url;
        }
        return httpUrl;
    }

    OkHttpClient generateOkClient() {
        if (timeout > 0) {
            return httpClient.newBuilder()
                    .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    .build();
        }
        return httpClient;
    }

    Request.Builder generateRequest() {
        Request.Builder builder = new Request.Builder();
        if (headers != null && !headers.isEmpty()) {
            builder.headers(Headers.of(headers));
        }
        if (tag != null) {
            builder.tag(tag);
        }
        return builder;
    }

    <R> Callback generateCallback(HttpCallback<R> callback) {
        Type dataType = TypeUtil.getGenericInterfaceTypeParameter(callback);
        return new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final HttpResponse resp = HttpResponse.processResponse(response);
                final String str = response.body().string();
                resp.data = str;
                if (response.isSuccessful()) {
                    if (dataType != null && !String.class.equals(dataType)) {
                        try {
                            resp.data = Parser.getParserFactory().fromJson(str, dataType);
                        } catch (Exception e) {
                            resp.error = new HttpResponse.Error(-102, e.getMessage());
                        }
                    }
                }
                onResult(resp);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                onResult(HttpResponse.error(-101, e.getMessage()));
            }

            private void onResult(HttpResponse httpResponse) {
                if (callback != null) {
                    if (executor != null) {
                        executor.execute(() -> callback.onResult(httpResponse));
                    } else {
                        callback.onResult(httpResponse);
                    }
                }
            }
        };
    }

}
