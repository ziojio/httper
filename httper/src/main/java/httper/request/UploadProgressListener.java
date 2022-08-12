package httper.request;

public interface UploadProgressListener {
    void onProgress(long uploadBytes, long totalBytes);
}