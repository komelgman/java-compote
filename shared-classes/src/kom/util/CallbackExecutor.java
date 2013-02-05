package kom.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class CallbackExecutor {

    private Executor threadPool = null;

    public <T> void execute(Callback<T> callback, T data) {
        callback.handle(data);

        if (callback instanceof Runnable) {
            if (threadPool != null) {
                threadPool.execute((Runnable)callback);
            } else {
                ((Runnable) callback).run();
            }
        }
    }

    public void setThreadPool(Executor threadPool) {
        this.threadPool = threadPool;
    }
}
