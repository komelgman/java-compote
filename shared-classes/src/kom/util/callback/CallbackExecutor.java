package kom.util.callback;

import java.util.concurrent.Executor;

public interface CallbackExecutor {
    public <T> void execute(Callback<T> callback, T data);
    public void setThreadExecutor(Executor threadExecutor);
}
