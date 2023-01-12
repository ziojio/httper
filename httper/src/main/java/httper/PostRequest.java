package httper;

import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PostRequest extends HttpRequest<PostRequest> {

    private final Map<String, String> formData = new HashMap<>();
    private String jsonBody;
    private RequestBody customBody;

    public PostRequest(Httper httper) {
        super(httper);
        if (httper.params != null) {
            this.formData.putAll(httper.params);
        }
    }

    public PostRequest addFormData(String name, String value) {
        this.formData.put(name, value);
        return this;
    }

    public PostRequest addFormData(Map<String, String> formData) {
        this.formData.putAll(formData);
        return this;
    }

    public PostRequest setJsonBody(Object json) {
        return setJsonBody(Parser.getParserFactory().toJson(json));
    }

    public PostRequest setJsonBody(String jsonBody) {
        if (jsonBody == null) throw new IllegalArgumentException("jsonBody == null");
        this.jsonBody = jsonBody;
        return this;
    }

    public PostRequest setCustomBody(RequestBody customBody) {
        if (jsonBody == null) throw new IllegalArgumentException("customBody == null");
        this.customBody = customBody;
        return this;
    }

    public <E> void request(HttpCallback<E> callback) {
        String httpUrl = generateUrl();

        Request.Builder builder = generateRequest().url(httpUrl);
        RequestBody body = generateRequestBody();
        Request request = builder.post(body).build();

        call = generateOkClient().newCall(request);
        call.enqueue(generateCallback(callback));
    }

    @Override
    protected RequestBody generateRequestBody() {
        RequestBody body;
        if (jsonBody != null) body = RequestBody.create(MEDIA_TYPE_JSON, jsonBody);
        else if (customBody != null) body = customBody;
        else body = createFormRequestBody(formData);
        return body;
    }

    private FormBody createFormRequestBody(Map<String, String> map) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }
}
