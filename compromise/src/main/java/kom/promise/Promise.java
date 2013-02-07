package kom.promise;

import kom.events.EventDispatcherImpl;
import kom.promise.events.*;
import kom.util.callback.Callback;
import kom.util.callback.CallbackExecutor;
import kom.util.pool.PoolableObject;
import kom.util.pool.TypedObjectPool;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class Promise<T> extends PoolableObject {
    private static final Timer scheduler = new Timer(true);
    private static final TypedObjectPool<PromiseEvent<Object>> eventPool
            = new TypedObjectPool<PromiseEvent<Object>>(128);

    private final EventDispatcherImpl<PromiseEvent> dispatcher = new EventDispatcherImpl<PromiseEvent>();
    private CallbackExecutor executor = null;

    private volatile boolean awaitFlag = true;
    private volatile boolean isFinished = false;
    private AtomicBoolean hasTimeout = new AtomicBoolean(false);

    private PromiseEvent<Object> reason = null;

    public Promise<T> success(Callback<? super SuccessEvent<T>> callback) {
        return custom(SuccessEvent.class, (Callback<? super SuccessEvent>)callback);
    }

    public Promise<T> fail(Callback<? super FailEvent> callback) {
        return custom(FailEvent.class, callback);
    }

    public Promise<T> progress(Callback<? super ProgressEvent> callback) {
        return custom(ProgressEvent.class, callback);
    }

    public Promise<T> halt(Callback<? super HaltEvent> callback) {
        return custom(HaltEvent.class, callback);
    }

    public Promise<T> always(Callback<? super PromiseEvent> callback) {
        return custom(PromiseEvent.class, callback);
    }

    public boolean cancel(Object data) {
        return notifyAll(HaltEvent.class, data, true);
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

        always(new Callback<PromiseEvent>() {
            @Override
            public void handle(PromiseEvent event) {
                task.cancel();
            }
        });

        scheduler.schedule(task, msecs);

        return this;
    }

    public synchronized Promise<T> await() {
        final Object awaiter = this;

        always(new Callback<PromiseEvent>() {
            @Override
            public void handle(PromiseEvent event) {
                if (event.getClass() == ProgressEvent.class) {
                    return;
                }

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

    synchronized <A extends PromiseEvent<Object>> boolean notifyAll(Class<A> reasonType, Object data, boolean finish) {
        if (isFinished) {
            // warn about notification on finished task
            System.out.println("Promise was notified with reason " + reasonType.getSimpleName());
            System.out.println("But this promise has already been stopped by reason "
                    + this.reason.getClass().getSimpleName());
            return false;
        }

        isFinished = finish;

        if (reason != null) {
            reason.release();
        }

        reason = eventPool.getObject(reasonType);
        reason.setData(data);

        dispatcher.dispatchEvent(reason);

        return true;
    }

    private synchronized <A extends PromiseEvent> Promise<T> custom(Class<A> reasonType, Callback<? super A> callback) {
        if (callback == null) {
            throw new NullPointerException("Callback can't be NULL");
        }

        if (isFinished) {
            if ((reasonType == PromiseEvent.class) || (reason.getClass() == reasonType)) {
                execute((Callback<PromiseEvent>)callback);
            }
        } else {
            dispatcher.addEventListener(reasonType, callback);
        }

        return this;
    }

    private void execute(Callback<PromiseEvent> callback) {
        if (executor == null) {
            callback.handle(reason);
        } else {
            executor.execute(callback, reason);
        }
    }

    @Override
    public void release() {
        reset();

        super.release();
    }

    public void reset() {
        if (reason != null) {
            reason.release();
        }

        dispatcher.setCallbackExecutor(null);
        executor = null;
        awaitFlag = true;
        hasTimeout.set(false);
        isFinished = false;
    }

    public void setCallbackExecutor(CallbackExecutor executor) {
        this.executor = executor;
        dispatcher.setCallbackExecutor(executor);
    }

    public PromiseEvent getReason() {
        return reason;
    }

    public T getResult() {
        if (reason instanceof SuccessEvent) {
            return ((SuccessEvent<T>)reason).getData();
        }

        throw new IllegalStateException("Result can be retrieved only if promise was successfully completed");
    }

    public boolean isFinished() {
        return isFinished;
    }
}