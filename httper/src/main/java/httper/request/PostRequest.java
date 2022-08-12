package httper.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import httper.HttpCallback;
import httper.Httper;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PostRequest extends HttpRequest<PostRequest> {
    private HashMap<String, String> formData;
    private String jsonBody;
    private String textBody;
    private RequestBody customBody;

    public PostRequest(Httper httper) {
        super(httper);
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

    public PostRequest setJsonBody(String jsonBody) {
        this.jsonBody = jsonBody;
        return this;
    }

    public PostRequest setTextBody(String textBody) {
        this.textBody = textBody;
        return this;
    }

    public PostRequest setCustomBody(RequestBody customBody) {
        this.customBody = customBody;
        return this;
    }

    public <R> void request(HttpCallback<R> callback) {
        String httpUrl = generateUrl();
        if (requestFilter != null && formData != null) {
            requestFilter.filter(httpUrl, formData);
        }
        Request.Builder builder = generateRequest().url(httpUrl);
        RequestBody body = null;
        if (formData != null) body = createFormRequestBody(formData);
        else if (jsonBody != null) body = RequestBody.create(MEDIA_TYPE_JSON, jsonBody);
        else if (textBody != null) body = RequestBody.create(MEDIA_TYPE_TEXT, textBody);
        else if (customBody != null) body = customBody;
        Objects.requireNonNull(body, "POST must have a request body.");

        Request request = builder.post(body).build();
        generateOkClient().newCall(request).enqueue(generateCallback(callback));
    }

    private FormBody createFormRequestBody(Map<String, String> map) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }
}
