package httper.request;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import httper.HttpCallback;
import httper.HttpMethod;
import httper.HttpResponse;
import httper.Httper;
import httper.Parser;
import httper.util.TypeUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @see GetRequest
 * @see PostRequest
 * @see UploadRequest
 * @see DownloadRequest
 */
@SuppressWarnings("unchecked")
public abstract class HttpRequest<T> {

    protected HttpMethod method;
    protected boolean debug;
    protected String baseUrl;
    protected OkHttpClient httpClient;
    protected Executor executor;
    protected Call call;

    protected String url;
    protected Object tag;
    protected long timeout;
    protected Map<String, String> headers;
    protected Map<String, String> params;

    public HttpRequest(Httper httper, HttpMethod method) {
        this.method = method;
        debug = httper.isDebug();
        baseUrl = httper.getBaseUrl();
        executor = httper.getCallbackExecutor();
        httpClient = httper.getHttpClient();

        if (httper.getHeaders() != null) {
            headers = new HashMap<>(httper.getHeaders());
        }
        if (httper.getParams() != null) {
            params = new HashMap<>(httper.getParams());
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

    public T executor(Executor executor) {
        if (executor != null) {
            this.executor = executor;
        }
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

    public T tag(Object tag) {
        this.tag = tag;
        return (T) this;
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
            call = null;
        }
    }

    protected RequestBody generateRequestBody() {
        return null;
    }

    protected String generateUrl() {
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

    protected OkHttpClient generateOkClient() {
        if (timeout > 0) {
            return httpClient.newBuilder()
                    .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    .build();
        }
        return httpClient;
    }

    protected Request.Builder generateRequest() {
        Request.Builder builder = new Request.Builder();
        if (headers != null && !headers.isEmpty()) {
            builder.headers(Headers.of(headers));
        }
        if (tag != null) {
            builder.tag(tag);
        }
        return builder;
    }

    protected <R> Callback generateCallback(HttpCallback<R> callback) {
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
