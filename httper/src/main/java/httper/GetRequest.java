package httper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class GetRequest extends HttpRequest<GetRequest> {

    private final Map<String, String> queryMap = new HashMap<>();

    public GetRequest(Httper httper) {
        super(httper);
        if (httper.params != null) {
            queryMap.putAll(httper.params);
        }
    }

    public GetRequest addQueryParameter(Map<String, String> map) {
        this.queryMap.putAll(map);
        return this;
    }

    public GetRequest addQueryParameter(String name, String value) {
        this.queryMap.put(name, value);
        return this;
    }

    public <E> void request(HttpCallback<E> callback) {
        String httpUrl = generateUrl();

        Request.Builder builder = generateRequest().url(httpUrl);
        Request request = builder.build();

        call = generateOkClient().newCall(request);
        call.enqueue(generateCallback(callback));
    }

    @Override
    protected String generateUrl() {
        String httpUrl = super.generateUrl();
        httpUrl = buildUrlWithParams(httpUrl, queryMap);
        return httpUrl;
    }

    private String buildUrlWithParams(String url, Map<String, String> map) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                urlBuilder.addQueryParameter(name, value);
            }
        }
        return urlBuilder.build().toString();
    }

}
