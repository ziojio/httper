package httper.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import httper.HttpMethod;
import httper.Httper;
import httper.Parser;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;


public class PostRequest extends HttpRequest<PostRequest> {
    static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private Map<String, String> formData;
    private String jsonBody;
    private RequestBody customBody;

    public PostRequest(Httper httper) {
        super(httper, HttpMethod.POST);
        if (params != null) {
            this.formData = new HashMap<>(params);
        }
    }

    public PostRequest addFormData(String name, String value) {
        if (this.formData == null) {
            this.formData = new HashMap<>();
        }
        this.formData.put(name, value);
        return this;
    }

    public PostRequest addFormData(Map<String, String> formData) {
        if (this.formData == null) {
            this.formData = new HashMap<>(formData);
        } else {
            this.formData.putAll(formData);
        }
        return this;
    }

    public PostRequest setJsonBody(Object json) {
        this.jsonBody = Parser.getParserFactory().toJson(json);
        return this;
    }

    public PostRequest setJsonBody(String jsonBody) {
        this.jsonBody = jsonBody;
        return this;
    }

    public PostRequest setCustomBody(RequestBody customBody) {
        this.customBody = customBody;
        return this;
    }

    @Override
    protected RequestBody generateRequestBody() {
        RequestBody body = null;
        if (formData != null) body = createFormRequestBody(formData);
        if (jsonBody != null) body = RequestBody.create(MEDIA_TYPE_JSON, jsonBody);
        if (customBody != null) body = customBody;
        return Objects.requireNonNull(body, "POST must have a request body.");
    }

    private FormBody createFormRequestBody(Map<String, String> map) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }
}
