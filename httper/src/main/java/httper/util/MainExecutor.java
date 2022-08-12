package httper.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

public class MainExecutor implements Executor {
    private final Handler handler;

    public MainExecutor() {
        handler = new Handler(Looper.getMainLooper());
    }

    public MainExecutor(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void execute(Runnable command) {
        if (command != null) {
            handler.post(command);
        }
    }

}
