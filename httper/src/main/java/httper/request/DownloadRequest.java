package httper.request;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import httper.HttpCallback;
import httper.HttpResponse;
import httper.Httper;
import httper.util.FileUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadRequest extends HttpRequest<DownloadRequest> {
    private String filePath;
    private Call call;
    private DownloadProgressListener downloadProgressListener;

    public DownloadRequest(Httper httper) {
        super(httper);
    }

    public DownloadRequest setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public DownloadRequest setFilePath(String dirPath, String filePath) {
        this.filePath = new File(dirPath, filePath).getAbsolutePath();
        return this;
    }

    public DownloadRequest setDownloadProgressListener(DownloadProgressListener listener) {
        this.downloadProgressListener = listener;
        return this;
    }

    public void request(HttpCallback<String> callback) {
        Request request = generateRequest().url(url).build();
        call = generateOkClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                final HttpResponse<String> resp = HttpResponse.processResponse(response);
                if (response.isSuccessful()) {
                    if (FileUtil.createOrExistsFile(filePath)) {
                        try {
                            writeFile(response.body());
                            resp.data = filePath;
                        } catch (Exception e) {
                            resp.error = new HttpResponse.Error(-104, e.getMessage());
                        }
                    } else {
                        resp.error = new HttpResponse.Error(-103, "file create failed.");
                    }
                }
                onResult(resp);
            }


            @Override
            public void onFailure(Call call, IOException e) {
                onResult(HttpResponse.error(-101, e.getMessage()));
            }

            private void onResult(HttpResponse httpResponse) {
                if (callback != null) {
                    if (executor != null) {
                        executor.execute(() -> callback.onResult(httpResponse));
                    } else {
                        callback.onResult(httpResponse);
                    }
                }
            }

            private void writeFile(ResponseBody body) throws IOException {
                InputStream inputStream = null;
                BufferedOutputStream outputStream = null;
                try {
                    inputStream = body.byteStream();
                    outputStream = new BufferedOutputStream(new FileOutputStream(filePath));

                    long total = body.contentLength();
                    long down = 0;
                    onProgress(0, total);

                    int len;
                    byte[] buf = new byte[4096];
                    long time;
                    long last = System.currentTimeMillis();
                    while ((len = inputStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, len);
                        down += len;
                        time = System.currentTimeMillis();
                        if (time - last > 250) {
                            onProgress(down, total);
                            last = time;
                        }
                    }
                    outputStream.flush();
                } finally {
                    FileUtil.closeIO(inputStream);
                    FileUtil.closeIO(outputStream);
                }
            }

            private void onProgress(long downloadBytes, long totalBytes) {
                if (downloadProgressListener != null) {
                    if (executor != null) {
                        executor.execute(() -> downloadProgressListener.onProgress(downloadBytes, totalBytes));
                    } else {
                        downloadProgressListener.onProgress(downloadBytes, totalBytes);
                    }
                }
            }
        });
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
            call = null;
        }
    }
}
