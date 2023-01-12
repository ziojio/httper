package httper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import httper.interceptor.LogInterceptor;
import httper.util.MainExecutor;
import httper.util.SSLUtil;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

public class Httper {

    boolean debug;
    String baseUrl;
    Map<String, String> headers;
    Map<String, String> params;
    Executor executor;
    OkHttpClient httpClient;

    Httper(Builder builder) {
        this.debug = builder.debug;
        this.baseUrl = builder.baseUrl;
        this.headers = builder.headers;
        this.params = builder.params;
        this.executor = builder.executor;
        this.httpClient = builder.httpClient;
    }

    public GetRequest get(String url) {
        return new GetRequest(this).url(url);
    }

    public PostRequest post(String url) {
        return new PostRequest(this).url(url);
    }

    public DownloadRequest download(String url) {
        return new DownloadRequest(this).url(url);
    }

    public UploadRequest upload(String url) {
        return new UploadRequest(this).url(url);
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

    public Map<String, String> getParams() {
        return params;
    }

    public Executor getExecutor() {
        return executor;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }


    public void cancel(Object tag) {
        for (Call call : httpClient.dispatcher().queuedCalls()) {
            if (call.request().tag() == tag) {
                call.cancel();
            }
        }
        for (Call call : httpClient.dispatcher().runningCalls()) {
            if (call.request().tag() == tag) {
                call.cancel();
            }
        }
    }

    public static class Builder {
        boolean debug;
        String baseUrl;
        Map<String, String> headers;
        Map<String, String> params;
        Executor executor;
        OkHttpClient httpClient;

        public Builder() {
            executor = new MainExecutor();
            httpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(SSLUtil.allowAllSSL(), new SSLUtil.EmptyX509TrustManager())
                    .build();
        }

        Builder(Httper httper) {
            this.debug = httper.debug;
            this.baseUrl = httper.baseUrl;
            this.headers = httper.headers != null ? new HashMap<>(httper.headers) : null;
            this.params = httper.params != null ? new HashMap<>(httper.params) : null;
            this.executor = httper.executor;
            this.httpClient = httper.httpClient;
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

        public Builder setParams(Map<String, String> params) {
            this.params = params;
            return this;
        }

        public Builder addParam(String key, String value) {
            if (this.params == null) {
                this.params = new HashMap<>();
            }
            this.params.put(key, value);
            return this;
        }

        public Builder setExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder setHttpClient(OkHttpClient httpClient) {
            if (httpClient == null) throw new IllegalArgumentException("httpClient == null");
            this.httpClient = httpClient;
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
            if (params != null) {
                params = Collections.unmodifiableMap(params);
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
