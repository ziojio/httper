package httper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import httper.interceptor.LogInterceptor;
import httper.request.DownloadRequest;
import httper.request.GetRequest;
import httper.request.PostRequest;
import httper.request.UploadRequest;
import httper.util.EmptyX509TrustManager;
import httper.util.MainExecutor;
import httper.util.SSLUtil;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

public class Httper {
    boolean debug;
    String baseUrl;
    Map<String, String> headers;
    Executor callbackExecutor;
    OkHttpClient httpClient;
    RequestFilter requestFilter;

    Httper(Builder builder) {
        this.debug = builder.debug;
        this.baseUrl = builder.baseUrl;
        this.headers = builder.headers;
        this.callbackExecutor = builder.callbackExecutor;
        this.httpClient = builder.httpClient;
        this.requestFilter = builder.requestFilter;
    }

    public GetRequest get() {
        return new GetRequest(this);
    }

    public PostRequest post() {
        return new PostRequest(this);
    }

    public DownloadRequest download() {
        return new DownloadRequest(this);
    }

    public UploadRequest upload() {
        return new UploadRequest(this);
    }


    public boolean isDebug() {
        return debug;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Executor getCallbackExecutor() {
        return callbackExecutor;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public RequestFilter getRequestFilter() {
        return requestFilter;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        boolean debug;
        String baseUrl;
        Map<String, String> headers;
        Executor callbackExecutor;
        OkHttpClient httpClient;
        RequestFilter requestFilter;

        public Builder() {
            callbackExecutor = new MainExecutor();
            httpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(SSLUtil.allowAllSSL(), new EmptyX509TrustManager())
                    .build();
        }

        Builder(Httper httper) {
            this.debug = httper.debug;
            this.baseUrl = httper.baseUrl;
            this.headers = httper.headers != null ? new HashMap<>(httper.headers) : null;
            this.callbackExecutor = httper.callbackExecutor;
            this.httpClient = httper.httpClient;
            this.requestFilter = httper.requestFilter;
        }

        public Builder setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder setBaseUrl(String baseUrl) {
            HttpUrl newUrl = HttpUrl.get(Objects.requireNonNull(baseUrl, "baseUrl == null"));
            List<String> pathSegments = newUrl.pathSegments();
            if (!"".equals(pathSegments.get(pathSegments.size() - 1))) {
                throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
            }
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder addHeader(String key, String value) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.put(key, value);
            return this;
        }

        public Builder setCallbackExecutor(Executor executor) {
            this.callbackExecutor = executor;
            return this;
        }

        public Builder setHttpClient(OkHttpClient httpClient) {
            if (httpClient == null) throw new IllegalArgumentException("httpClient == null");
            this.httpClient = httpClient;
            return this;
        }

        public Builder setRequestFilter(RequestFilter requestFilter) {
            this.requestFilter = requestFilter;
            return this;
        }

        public Httper build() {
            if (debug) {
                if (!hasLogInterceptor()) addLogInterceptor();
            } else {
                if (hasLogInterceptor()) removeLogInterceptor();
            }
            if (headers != null) {
                headers = Collections.unmodifiableMap(headers);
            }
            return new Httper(this);
        }

        private boolean hasLogInterceptor() {
            List<Interceptor> list = httpClient.interceptors();
            for (Interceptor interceptor : list) {
                if (interceptor instanceof LogInterceptor) {
                    return true;
                }
            }
            return false;
        }

        private void addLogInterceptor() {
            OkHttpClient.Builder builder = httpClient.newBuilder();
            builder.addInterceptor(new LogInterceptor());
            httpClient = builder.build();
        }

        private void removeLogInterceptor() {
            OkHttpClient.Builder builder = httpClient.newBuilder();
            Iterator<Interceptor> iterator = builder.interceptors().iterator();
            while (iterator.hasNext()) {
                if (iterator.next() instanceof LogInterceptor) {
                    iterator.remove();
                }
            }
            httpClient = builder.build();
        }
    }
}
