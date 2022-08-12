package httper.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import httper.HttpCallback;
import httper.Httper;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class GetRequest extends HttpRequest<GetRequest> {
    private final HashMap<String, String> queryMap = new HashMap<>();

    public GetRequest(Httper config) {
        super(config);
    }

    public GetRequest addQueryParameter(Map<String, String> map) {
        this.queryMap.putAll(map);
        return this;
    }

    public GetRequest addQueryParameter(String name, String value) {
        this.queryMap.put(name, value);
        return this;
    }

    public <R> void request(HttpCallback<R> callback) {
        String httpUrl = generateUrl();
        if (requestFilter != null) {
            requestFilter.filter(httpUrl, queryMap);
        }
        httpUrl = buildUrlWithParams(httpUrl, queryMap);

        Request request = generateRequest().url(httpUrl).build();
        generateOkClient().newCall(request).enqueue(generateCallback(callback));
    }

    private String buildUrlWithParams(String url, Map<String, String> map) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();
                urlBuilder.addQueryParameter(name, String.valueOf(value));
            }
        }
        return urlBuilder.build().toString();
    }

}
