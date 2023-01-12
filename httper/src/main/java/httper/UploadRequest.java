package httper;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UploadRequest extends HttpRequest<UploadRequest> {

    private final Map<String, String> formData = new HashMap<>();
    private final Map<String, RequestBody> bodyMap = new HashMap<>();
    private final Map<String, List<FileBody>> fileBodyMap = new HashMap<>();

    private UploadProgressListener listener;

    public UploadRequest(Httper httper) {
        super(httper);
        if (httper.params != null) {
            this.formData.putAll(httper.params);
        }
    }

    private static String getMimeType(String fileName) {
        String contentTypeFor = URLConnection.getFileNameMap().getContentTypeFor(fileName);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    public UploadRequest addFormData(String name, String value) {
        this.formData.put(name, value);
        return this;
    }

    public UploadRequest addFormData(Map<String, String> formData) {
        this.formData.putAll(formData);
        return this;
    }

    public UploadRequest addFile(String name, File file) {
        return addFile(name, file, null);
    }

    public UploadRequest addFile(String name, File file, String contentType) {
        List<FileBody> fileBodies = fileBodyMap.get(name);
        if (fileBodies == null) {
            fileBodies = new ArrayList<>();
        }
        fileBodies.add(new FileBody(file, contentType));
        fileBodyMap.put(name, fileBodies);
        return this;
    }

    public UploadRequest addBody(String name, RequestBody body) {
        this.bodyMap.put(name, body);
        return this;
    }

    public UploadRequest setUploadProgressListener(UploadProgressListener listener) {
        this.listener = listener;
        return this;
    }

    private void addFormData(MultipartBody.Builder body) {
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            body.addFormDataPart(entry.getKey(), entry.getValue());
        }
    }

    public <E> void request(HttpCallback<E> callback) {
        String httpUrl = generateUrl();

        Request.Builder builder = generateRequest().url(httpUrl);
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        addFormData(bodyBuilder);
        addBody(bodyBuilder);
        addFileBody(bodyBuilder);
        RequestBody body = bodyBuilder.build();
        if (listener != null) {
            UploadProgressListener l = executor != null
                    ? (uploadBytes, totalBytes) -> executor.execute(() -> listener.onProgress(uploadBytes, totalBytes))
                    : (uploadBytes, totalBytes) -> listener.onProgress(uploadBytes, totalBytes);
            body = new UploadBody(body, l);
        }
        Request request = builder.post(body).build();

        call = generateOkClient().newCall(request);
        call.enqueue(generateCallback(callback));
    }

    private void addBody(MultipartBody.Builder body) {
        for (Map.Entry<String, RequestBody> entry : bodyMap.entrySet()) {
            body.addFormDataPart(entry.getKey(), null, entry.getValue());
        }
    }

    private void addFileBody(MultipartBody.Builder body) {
        for (Map.Entry<String, List<FileBody>> entry : fileBodyMap.entrySet()) {
            List<FileBody> fileBodies = entry.getValue();
            for (FileBody fileBody : fileBodies) {
                String fileName = fileBody.file.getName();
                MediaType mediaType = fileBody.contentType != null
                        ? MediaType.parse(fileBody.contentType)
                        : MediaType.parse(getMimeType(fileName));
                RequestBody requestBody = RequestBody.create(mediaType, fileBody.file);
                body.addFormDataPart(entry.getKey(), fileName, requestBody);
            }
        }
    }

    static class FileBody {
        public File file;
        public String contentType;

        public FileBody(File file, String contentType) {
            this.file = file;
            this.contentType = contentType;
        }
    }
}
