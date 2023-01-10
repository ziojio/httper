package httper.request;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import httper.HttpCallback;
import httper.HttpMethod;
import httper.Httper;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UploadRequest extends HttpRequest<UploadRequest> {
    private final HashMap<String, String> formData = new HashMap<>();
    private final HashMap<String, StringBody> stringBodyMap = new HashMap<>();
    private final HashMap<String, List<FileBody>> fileBodyMap = new HashMap<>();

    private UploadProgressListener uploadProgressListener;

    public UploadRequest(Httper httper) {
        super(httper, HttpMethod.POST);
    }

    private static String getMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
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

    public UploadRequest addFormData(String name, String value) {
        this.formData.put(name, value);
        return this;
    }

    public UploadRequest addFormData(Map<String, String> formData) {
        this.formData.putAll(formData);
        return this;
    }

    public UploadRequest addFormData(String name, String value, String contentType) {
        this.stringBodyMap.put(name, new StringBody(value, contentType));
        return this;
    }

    public UploadRequest setUploadProgressListener(UploadProgressListener listener) {
        this.uploadProgressListener = listener;
        return this;
    }

    public <R> void request(HttpCallback<R> callback) {
        String httpUrl = generateUrl();

        Request.Builder builder = generateRequest().url(httpUrl);
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        addFormData(bodyBuilder);
        addStringBody(bodyBuilder);
        addFileBody(bodyBuilder);
        RequestBody body = bodyBuilder.build();
        if (uploadProgressListener != null) {
            body = new MultipartProgressBody(body, (uploadBytes, totalBytes) -> {
                if (executor != null) {
                    executor.execute(() -> uploadProgressListener.onProgress(uploadBytes, totalBytes));
                } else {
                    uploadProgressListener.onProgress(uploadBytes, totalBytes);
                }
            });
        }
        Request request = builder.post(body).build();

        call = generateOkClient().newCall(request);
        call.enqueue(generateCallback(callback));
    }

    private void addFormData(MultipartBody.Builder body) {
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            body.addFormDataPart(entry.getKey(), entry.getValue());
        }
    }

    private void addStringBody(MultipartBody.Builder body) {
        for (Map.Entry<String, StringBody> entry : stringBodyMap.entrySet()) {
            StringBody stringBody = entry.getValue();
            MediaType mediaType = null;
            if (stringBody.contentType != null) {
                mediaType = MediaType.parse(stringBody.contentType);
            }
            body.addFormDataPart(entry.getKey(), null,
                    RequestBody.create(mediaType, stringBody.data));
        }
    }

    private void addFileBody(MultipartBody.Builder body) {
        for (Map.Entry<String, List<FileBody>> entry : fileBodyMap.entrySet()) {
            List<FileBody> fileBodies = entry.getValue();
            for (FileBody fileBody : fileBodies) {
                String fileName = fileBody.file.getName();
                MediaType mediaType;
                if (fileBody.contentType != null) {
                    mediaType = MediaType.parse(fileBody.contentType);
                } else {
                    mediaType = MediaType.parse(getMimeType(fileName));
                }
                RequestBody requestBody = RequestBody.create(mediaType, fileBody.file);
                body.addFormDataPart(entry.getKey(), fileName, requestBody);
            }
        }
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
            call = null;
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

    static class StringBody {
        public String data;
        public String contentType;

        public StringBody(String data, String contentType) {
            this.data = data;
            this.contentType = contentType;
        }
    }
}
