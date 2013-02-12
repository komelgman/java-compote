package kom.promise;

import kom.events.EventDispatcherImpl;
import kom.promise.events.*;
import kom.promise.util.PromiseEnvironment;
import kom.util.callback.Callback;

import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class Promise<T> {
    private final EventDispatcherImpl<PromiseEvent> dispatcher = new EventDispatcherImpl<PromiseEvent>();

    private volatile boolean awaitFlag = true;
    private volatile boolean isFinished = false;
    private AtomicBoolean hasTimeout = new AtomicBoolean(false);

    private PromiseEvent<Object> reason = null;
    private PromiseEnvironment environment;
    private Object tag = null;

    public Promise<T> onSuccess(Callback<? super SuccessEvent<T>> callback) {
        return custom(SuccessEvent.class, (Callback<? super SuccessEvent>)callback);
    }

    public Promise<T> onFail(Callback<? super FailEvent> callback) {
        return custom(FailEvent.class, callback);
    }

    public Promise<T> onUpdate(Callback<? super UpdateEvent> callback) {
        return custom(UpdateEvent.class, callback);
    }

    public Promise<T> onAbort(Callback<? super AbortEvent> callback) {
        return custom(AbortEvent.class, callback);
    }

    public Promise<T> onAny(Callback<? super PromiseEvent> callback) {
        return custom(PromiseEvent.class, callback);
    }

    public boolean abort(Object data) {
        return notifyAll(AbortEvent.class, data, true);
    }

    public Promise<T> timeout(int msecs) {
        if (hasTimeout.getAndSet(true)) {
            throw new IllegalStateException("Promise has already been timeout");
        }

        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Promise.this.abort(new TimeoutException("Promise was cancelled by timeout"));
            }
        };

        onAny(new Callback<PromiseEvent>() {
            @Override
            public void handle(PromiseEvent event) {
                if (event.getClass() == UpdateEvent.class) {
                    return;
                }


                task.cancel();
            }
        });

        getEnvironment().getScheduler().schedule(task, msecs);

        return this;
    }

    public synchronized Promise<T> await() {
        final Object awaiter = this;

        onAny(new Callback<PromiseEvent>() {
            @Override
            public void handle(PromiseEvent event) {
                if (event.getClass() == UpdateEvent.class) {
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

        reason = getEnvironment().getEvent(reasonType);
        reason.setData(data);

        dispatcher.dispatchEvent(reason);

        return true;
    }

    private synchronized <A extends PromiseEvent> Promise<T> custom(Class<A> reasonType, Callback<? super A> callback) {
        if (callback == null) {
            throw new NullPointerException("Callback can't be NULL");
        }

        if (isFinished) {
            execute(reasonType, (Callback<PromiseEvent>) callback);
        } else {
            dispatcher.addEventListener(reasonType, callback);
        }

        return this;
    }

    private <A extends PromiseEvent> void execute(Class<A> reasonType, Callback<PromiseEvent> callback) {
        if ((reasonType == PromiseEvent.class) || (reason.getClass() == reasonType)) {
            getEnvironment().executeCallback(callback, reason);
        }
    }

    protected synchronized PromiseEnvironment getEnvironment() {
        if (environment == null) {
            setEnvironment(PromiseEnvironment.getDefaultEnvironment());
        }

        return environment;
    }

    public synchronized void setEnvironment(PromiseEnvironment environment) {
        this.environment = environment;
        dispatcher.setCallbackExecutor(environment.getCallbackExecutor());
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

    public Object getTag() {
        return tag;
    }

    public Promise<T> setTag(Object value) {
        tag = value;
        return this;
    }

    public void reset() {
        awaitFlag = true;
        hasTimeout.set(false);
        isFinished = false;
        reason = null;
        tag = null;

        dispatcher.setCallbackExecutor(null);
        dispatcher.removeEventListeners();
        environment = null;
    }
}