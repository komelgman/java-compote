package kom.promise;

import kom.events.Event;
import kom.events.EventDispatcherImpl;
import kom.util.Callback;
import kom.util.CallbackExecutor;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: komelgman
 * Date: 9/4/12
 * Time: 11:51 AM
 */
public class Promise<T> {
    private static final Timer scheduler = new Timer(true);

    private final EventDispatcherImpl<Event> dispatcher = new EventDispatcherImpl<Event>();

    private volatile boolean awaitFlag = true;
    private AtomicBoolean hasTimeout = new AtomicBoolean(false);
    private boolean isFinished = false;

    private Event reason = null;
    private CallbackExecutor executor = new CallbackExecutor();

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
        return custom(Event.class, callback);
    }

    public boolean cancel(Object data) {
        return notifyAll(PromiseEventFactory.getCancelEvent(data), true);
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

    synchronized boolean notifyAll(Event reason, boolean finish) {
        if (isFinished) {
            // warn about notification on finished task
            System.out.println("Promise was notified with reason " + reason.toString());
            System.out.println("But this promise has already been stopped by reason " + this.reason.toString());
            return false;
        }

        this.isFinished = finish;
        this.reason = reason;

        dispatcher.dispatchEvent(reason);

        return true;
    }

    @SuppressWarnings("unchecked")
    private synchronized Promise<T> custom(Class<? extends Event> reasonType, Callback callback) {
        if (callback == null) {
            throw new NullPointerException("Callback can't be NULL");
        }

        if (isFinished) {
            if ((reasonType == Event.class) || (this.reason.getClass() == reasonType)) {
                executor.execute(callback, reason);
            }
        } else {
            dispatcher.addEventListener(reasonType, callback);
        }

        return this;
    }

    /*
     * GETTERS/SETTERS
     */

    public void setThreadPool(Executor threadPool) {
        executor.setThreadPool(threadPool);
        dispatcher.setThreadPool(threadPool);
    }

    public Event getReason() {
        return reason;
    }

    @SuppressWarnings("unchecked")
    public T getResult() {
        if (reason instanceof SuccessEvent) {
            return ((SuccessEvent<T>) reason).data;
        }

        throw new IllegalStateException("Result can be retrieved only if promise was successfully completed");
    }

    public boolean isFinished() {
        return isFinished;
    }
}