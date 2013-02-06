package kom.promise;

import kom.events.EventDispatcherImpl;
import kom.promise.events.*;
import kom.util.*;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class Promise<T> extends PoolableObject {
    private static final Timer scheduler = new Timer(true);
    private static final TypedObjectPool<PromiseEvent> eventPool = new TypedObjectPool<PromiseEvent>(128);

    private final EventDispatcherImpl<PromiseEvent> dispatcher = new EventDispatcherImpl<PromiseEvent>();
    private CallbackExecutor executor = null;

    private volatile boolean awaitFlag = true;
    private AtomicBoolean hasTimeout = new AtomicBoolean(false);
    private boolean isFinished = false;

    private PromiseEvent reason = null;

    public Promise<T> success(Callback<T> callback) {
        return custom(SuccessEvent.class, callback);
    }

    public Promise<T> fail(Callback callback) {
        return custom(FailEvent.class, callback);
    }

    public Promise<T> progress(Callback<T> callback) {
        return custom(ProgressEvent.class, callback);
    }

    public Promise<T> halt(Callback callback) {
        return custom(CancelEvent.class, callback);
    }

    public Promise<T> always(Callback callback) {
        return custom(PromiseEvent.class, callback);
    }

    public boolean cancel(Object data) {
        return notifyAll(CancelEvent.class, data, true);
    }

    public Promise<T> timeout(int msecs) {
        if (hasTimeout.getAndSet(true)) {
            throw new IllegalStateException("Promise has already been timeout");
        }

        final Promise<T> promise = this;
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                promise.cancel(new TimeoutException("Promise was cancelled by timeout"));
            }
        };

        always(new Callback<Object>() {
            @Override
            public void handle(Object data) {
                task.cancel();
            }
        });

        scheduler.schedule(task, msecs);

        return this;
    }

    public synchronized Promise<T> await() {
        final Object awaiter = this;

        always(new Callback<Object>() {
            @Override
            public void handle(Object data) {
                awaitFlag = false;

                synchronized (awaiter) {
                    awaiter.notify();
                }
            }
        });

        while (awaitFlag) {
            try {
                wait();
            } catch (InterruptedException e) {
                // nothing
            }
        }

        return this;
    }

    synchronized boolean notifyAll(Class<? extends PromiseEvent> event, Object data, boolean finish) {
        if (isFinished) {
            // warn about notification on finished task
            System.out.println("Promise was notified with reason " + event.getSimpleName());
            System.out.println("But this promise has already been stopped by reason "
                    + this.reason.getClass().getSimpleName());
            return false;
        }

        isFinished = finish;

        if (reason != null) {
            reason.release();
        }

        reason = eventPool.getObject(event);
        reason.setData(data);

        dispatcher.dispatchEvent(reason);

        return true;
    }

    private synchronized Promise<T> custom(Class<? extends PromiseEvent> reasonType, Callback callback) {
        if (callback == null) {
            throw new NullPointerException("Callback can't be NULL");
        }

        if (isFinished) {
            if ((reasonType == PromiseEvent.class) || (this.reason.getClass() == reasonType)) {
                executor.execute(callback, reason);
            }
        } else {
            dispatcher.addEventListener(reasonType, callback);
        }

        return this;
    }

    @Override
    public void release() {
        if (reason != null) {
            reason.release();
        }

        super.release();
    }

    /*
     * GETTERS/SETTERS
     */

    public void setCallbackExecutor(CallbackExecutor executor) {
        this.executor = executor;
        dispatcher.setCallbackExecutor(executor);
    }

    public PromiseEvent getReason() {
        return reason;
    }

    public T getResult() {
        if (reason instanceof SuccessEvent) {
            return (T)reason.getData();
        }

        throw new IllegalStateException("Result can be retrieved only if promise was successfully completed");
    }

    public boolean isFinished() {
        return isFinished;
    }
}