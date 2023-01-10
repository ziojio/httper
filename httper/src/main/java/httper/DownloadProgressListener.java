package httper;

public interface DownloadProgressListener {
    void onProgress(long downloadBytes, long totalBytes);
}
