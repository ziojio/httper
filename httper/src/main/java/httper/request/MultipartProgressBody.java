package httper.request;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public class MultipartProgressBody extends RequestBody {
    private final UploadProgressListener uploadListener;
    private final RequestBody requestBody;
    private BufferedSink bufferedSink;

    public MultipartProgressBody(RequestBody requestBody, UploadProgressListener uploadListener) {
        this.requestBody = requestBody;
        this.uploadListener = uploadListener;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));
        }
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;
                if (uploadListener != null) {
                    uploadListener.onProgress(bytesWritten, contentLength);
                }
            }
        };
    }

}