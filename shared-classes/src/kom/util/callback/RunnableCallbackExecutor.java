package kom.util.callback;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableCallbackExecutor implements CallbackExecutor {
    private static final Logger log = Logger.getLogger(RunnableCallbackExecutor.class.getName());

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
            log.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @Override
    public void setRunnableExecutor(Executor runnableExecutor) {
        this.executor = runnableExecutor;
    }

    public static CallbackExecutor getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    public static class SingletonHolder {
        public static final CallbackExecutor HOLDER_INSTANCE = new RunnableCallbackExecutor();
    }
}
