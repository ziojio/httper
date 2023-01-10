package httper;

public interface UploadProgressListener {
    void onProgress(long uploadBytes, long totalBytes);
}