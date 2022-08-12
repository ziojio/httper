package httper.request;

public interface DownloadProgressListener {
    void onProgress(long downloadBytes, long totalBytes);
}
