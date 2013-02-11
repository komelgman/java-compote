package kom.promise.util;

import kom.promise.Deferred;
import kom.promise.Promise;
import kom.promise.events.AbortEvent;
import kom.util.callback.Callback;

@SuppressWarnings("UnusedDeclaration")
public abstract class AsyncTask<T> extends Promise<T> {

    private final Deferred<T> deferred;

    public AsyncTask() {
        this.deferred = new Deferred<T>(this, new TaskCanceller());
    }

    public final AsyncTask<T> start() {
        getEnvironment().executeRunnable(new TaskProcessor());

        return this;
    }

    protected final void resolve(T data) {
        deferred.resolve(data);
    }

    protected final void reject(Object data) {
        deferred.reject(data);
    }

    protected final void update(Object data) {
        deferred.update(data);
    }

    protected abstract void process();
    protected /* virtual */ void canceller() {}



    private class TaskProcessor implements Runnable {
        @Override
        public void run() {
            AsyncTask.this.process();
        }
    }

    private class TaskCanceller implements Callback<AbortEvent> {
        @Override
        public void handle(AbortEvent message) {
            AsyncTask.this.canceller();
        }
    }
}