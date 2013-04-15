/*
 * Copyright 2013 Sergey Yungman (aka komelgman)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kom.promise.impl;

import kom.events.DefaultEventDispatcher;
import kom.events.EventDispatcher;
import kom.promise.Promise;
import kom.promise.events.*;
import kom.promise.exceptions.PromiseException;
import kom.promise.util.AsyncContext;
import kom.util.callback.Callback;

import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static kom.promise.util.AsyncUtils.defaultContext;

@SuppressWarnings("unchecked")
public class PromiseImpl<T> implements Promise<T> {
    private static final Logger log = Logger.getLogger(Promise.class.getName());

    private final EventDispatcher<PromiseEvent> dispatcher = new DefaultEventDispatcher<PromiseEvent>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition awaiter = lock.newCondition();

    private final AtomicReference<TimerTask> timerTask = new AtomicReference<TimerTask>(null);
    private final AtomicReference<AsyncContext> context = new AtomicReference<AsyncContext>(null);

    private AtomicBoolean isDone = new AtomicBoolean(false);
    private volatile PromiseEvent<Object> doneEvent = null;
    private volatile Object tag = null;

    public PromiseImpl() {
        this(null);
    }

    public PromiseImpl(AsyncContext context) {
        setContext(context);
    }

    public void setContext(AsyncContext context) {
        if (context == null) {
            this.context.set(defaultContext());
        } else {
            this.context.set(context);
        }

        dispatcher.setCallbackExecutor(this.context.get().getCallbackExecutor());
    }



    @Override
    public Promise<T> onSuccess(Callback<? super SuccessEvent<T>> callback) {
        return attachCallback(SuccessEvent.class, (Callback<? super SuccessEvent>) callback);
    }

    @Override
    public Promise<T> onFail(Callback<? super FailEvent> callback) {
        return attachCallback(FailEvent.class, callback);
    }

    @Override
    public Promise<T> onUpdate(Callback<? super UpdateEvent> callback) {
        return attachCallback(UpdateEvent.class, callback);
    }

    @Override
    public Promise<T> onCancel(Callback<? super CancelEvent> callback) {
        return attachCallback(CancelEvent.class, callback);
    }

    @Override
    public Promise<T> onAny(Callback<? super PromiseEvent> callback) {
        return attachCallback(PromiseEvent.class, callback);
    }



    @Override
    public boolean cancel(Object data) {
        return triggerEventAndStopProcessing(CancelEvent.class, data);
    }

    @Override
    public boolean cancel() {
        return cancel(null);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return cancel(null);
    }

    @Override
    public Promise<T> timeout(long msecs) {
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                PromiseImpl.this.cancel(new TimeoutException("Promise was cancelled by timeout"));
            }
        };

        if (!timerTask.compareAndSet(null, task)) {
            throw new IllegalStateException("Promise has already been timeout");
        }

        onAny(new Callback<PromiseEvent>() {
            @Override
            public void handle(PromiseEvent event) {
                if (event.getClass() == UpdateEvent.class) {
                    return;
                }

                timerTask.get().cancel();
            }
        });

        context().getScheduler().schedule(timerTask.get(), msecs);

        return this;
    }

    @Override
    public Promise<T> await() {
        if (!isDone.get()) {
            waitForTaskToBeCompleted();
        }

        return this;
    }

    private void waitForTaskToBeCompleted() {
        lock.lock();
        try {
            while (!isDone.get()) {
                awaiter.awaitUninterruptibly();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T tryGet() {
        if (isSuccessed()) {
            return ((SuccessEvent<T>) doneEvent).getData();
        }

        return null;
    }

    @Override
    public Object rawGet() {
        if (isDone()) {
            return doneEvent.getData();
        }

        throw new IllegalStateException("Result can be retrieved only if promise was completed");
    }

    @Override
    public T get() throws ExecutionException, InterruptedException {
        if (isSuccessed()) {
            return ((SuccessEvent<T>) doneEvent).getData();
        }

        final Object data = doneEvent.getData();
        if (data instanceof InterruptedException)
            throw (InterruptedException)data;

        if (data instanceof ExecutionException)
            throw (ExecutionException)data;

        if (data instanceof TimeoutException)
            throw new ExecutionException((TimeoutException)data);

        if (isCancelled()) {
            throw new ExecutionException(new PromiseException(CancelEvent.class, doneEvent.getData()));
        }

        if (isFailed()) {
            throw new ExecutionException(new PromiseException(FailEvent.class, doneEvent.getData()));
        }

        throw new IllegalStateException("Result can be retrieved only if promise was completed");
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return timeout(unit.convert(timeout, TimeUnit.MILLISECONDS)).get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TimeoutException) {
                throw (TimeoutException)e.getCause();
            }

            throw e;
        }
    }

    protected <A extends PromiseEvent<Object>> boolean triggerEvent(Class<A> reasonType, Object data) {
        if (isDone.get()) {
            warningAboutCompletedTask(reasonType);
            return false;
        }

        final PromiseEvent reason = context().event(reasonType);
        reason.setData(data);

        dispatcher.dispatchEvent(reason);

        return true;
    }

    protected <A extends PromiseEvent<Object>> boolean triggerEventAndStopProcessing(Class<A> reasonType, Object data) {
        if (!isDone.compareAndSet(false, true)) {
            warningAboutCompletedTask(reasonType);
            return false;
        }

        doneEvent = context().event(reasonType);
        doneEvent.setData(data);

        dispatcher.dispatchEvent(doneEvent);

        notifyAboutTaskCompleted();

        return true;
    }

    private <A extends PromiseEvent<Object>> void warningAboutCompletedTask(Class<A> reasonType) {
        log.log(Level.WARNING, "Promise was notified with reason " + reasonType.getSimpleName() + "\n"
                + "But this promise has already been stopped by reason "
                + this.doneEvent.getClass().getSimpleName());
    }

    private void notifyAboutTaskCompleted() {
        lock.lock();
        try {
            awaiter.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private <A extends PromiseEvent> Promise<T> attachCallback(Class<A> observedEvent, Callback<? super A> callback) {
        if (callback == null) {
            throw new NullPointerException("Callback can't be NULL");
        }

        if (isDone.get()) {
            executeCallback(observedEvent, (Callback<PromiseEvent>) callback);
        } else {
            dispatcher.addEventListener(observedEvent, callback);
        }

        return this;
    }

    private <A extends PromiseEvent> void executeCallback(Class<A> observedEvent, Callback<PromiseEvent> callback) {
        if ((observedEvent == PromiseEvent.class) || (doneEvent.getClass() == observedEvent)) {
            context().executeCallback(callback, doneEvent);
        }
    }

    protected AsyncContext context() {
        return context.get();
    }

    @Override
    public boolean isDone() {
        return isDone.get();
    }

    @Override
    public boolean isCancelled() {
        return isDone.get() && doneEvent.getClass() == CancelEvent.class;
    }

    @Override
    public boolean isSuccessed() {
        return isDone.get() && doneEvent.getClass() == SuccessEvent.class;
    }

    @Override
    public boolean isFailed() {
        return isDone.get() && doneEvent.getClass() == FailEvent.class;
    }

    @Override
    public Promise<T> setTag(Object value) {
        tag = value;
        return this;
    }

    @Override
    public Object getTag() {
        return tag;
    }
}
