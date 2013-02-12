package kom.util.callback;

import java.util.concurrent.Executor;

public class RunnableCallbackExecutor implements CallbackExecutor {
    private volatile Executor executor = null;

    public RunnableCallbackExecutor() {
    }

    public RunnableCallbackExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public <T> void execute(Callback<T> callback, T data) {
        try {
            callback.handle(data);

            if (callback instanceof Runnable) {
                if (executor != null) {
                    executor.execute((Runnable)callback);
                } else {
                    ((Runnable) callback).run();
                }
            }
        } catch (Exception e) {
            // warning
        }
    }

    @Override
    public void setThreadExecutor(Executor threadExecutor) {
        this.executor = threadExecutor;
    }

    public static CallbackExecutor getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    public static class SingletonHolder {
        public static final CallbackExecutor HOLDER_INSTANCE = new RunnableCallbackExecutor();
    }
}
