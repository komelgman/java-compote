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

package kom.promise;

import kom.events.EventDispatcherImpl;
import kom.promise.events.*;
import kom.promise.util.PromiseEnvironment;
import kom.util.callback.Callback;

import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class Promise<T> {
    private static final Logger log = Logger.getLogger(Promise.class.getName());

    private final EventDispatcherImpl<PromiseEvent> dispatcher = new EventDispatcherImpl<PromiseEvent>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition awaiter = lock.newCondition();

    private final AtomicReference<TimerTask> timerTask = new AtomicReference<TimerTask>(null);
    private final AtomicReference<PromiseEnvironment> environment = new AtomicReference<PromiseEnvironment>(null);

    private AtomicBoolean isComplete = new AtomicBoolean(false);
    private volatile PromiseEvent<Object> reasonOfTaskCompletion = null;
    private volatile Object tag = null;


    public Promise() {
        setEnvironment(null);
    }

    public Promise<T> onSuccess(Callback<? super SuccessEvent<T>> callback) {
        return attachCallback(SuccessEvent.class, (Callback<? super SuccessEvent>) callback);
    }

    public Promise<T> onFail(Callback<? super FailEvent> callback) {
        return attachCallback(FailEvent.class, callback);
    }

    public Promise<T> onUpdate(Callback<? super UpdateEvent> callback) {
        return attachCallback(UpdateEvent.class, callback);
    }

    public Promise<T> onAbort(Callback<? super AbortEvent> callback) {
        return attachCallback(AbortEvent.class, callback);
    }

    public Promise<T> onAny(Callback<? super PromiseEvent> callback) {
        return attachCallback(PromiseEvent.class, callback);
    }

    public boolean abort(Object data) {
        return signalAboutCompletion(AbortEvent.class, data);
    }


    public Promise<T> timeout(int msecs) {
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Promise.this.abort(new TimeoutException("Promise was cancelled by timeout"));
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

        getEnvironment().getScheduler().schedule(timerTask.get(), msecs);

        return this;
    }

    public Promise<T> await() {
        if (!isComplete.get()) {
            waitForTaskToBeCompleted();
        }

        return this;
    }

    private void waitForTaskToBeCompleted() {
        lock.lock();
        try {
            while (!isComplete.get()) {
                awaiter.awaitUninterruptibly();
            }
        } finally {
            lock.unlock();
        }
    }

    <A extends PromiseEvent<Object>> boolean signalAboutProgress(Class<A> reasonType, Object data) {
        if (isComplete.get()) {
            warningAboutCompletedTask(reasonType);
            return false;
        }

        final PromiseEvent reason = getEnvironment().getEvent(reasonType);
        reason.setData(data);

        dispatcher.dispatchEvent(reason);

        return true;
    }

    <A extends PromiseEvent<Object>> boolean signalAboutCompletion(Class<A> reasonType, Object data) {
        if (!isComplete.compareAndSet(false, true)) {
            warningAboutCompletedTask(reasonType);
            return false;
        }

        reasonOfTaskCompletion = getEnvironment().getEvent(reasonType);
        reasonOfTaskCompletion.setData(data);

        dispatcher.dispatchEvent(reasonOfTaskCompletion);

        notifyAboutTaskCompleted();

        return true;
    }

    private <A extends PromiseEvent<Object>> void warningAboutCompletedTask(Class<A> reasonType) {
        log.log(Level.WARNING, "Promise was notified with reason " + reasonType.getSimpleName() + "\n"
                + "But this promise has already been stopped by reason "
                + this.reasonOfTaskCompletion.getClass().getSimpleName());
    }

    private void notifyAboutTaskCompleted() {
        lock.lock();
        try {
            awaiter.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private <A extends PromiseEvent> Promise<T> attachCallback(Class<A> reasonType, Callback<? super A> callback) {
        if (callback == null) {
            throw new NullPointerException("Callback can't be NULL");
        }

        if (isComplete.get()) {
            executeCallback(reasonType, (Callback<PromiseEvent>) callback);
        } else {
            dispatcher.addEventListener(reasonType, callback);
        }

        return this;
    }

    private <A extends PromiseEvent> void executeCallback(Class<A> reasonType, Callback<PromiseEvent> callback) {
        if ((reasonType == PromiseEvent.class) || (reasonOfTaskCompletion.getClass() == reasonType)) {
            getEnvironment().executeCallback(callback, reasonOfTaskCompletion);
        }
    }

    public void setEnvironment(PromiseEnvironment value) {
        if (value == null) {
            this.environment.set(PromiseEnvironment.getDefaultEnvironment());
        } else {
            this.environment.set(value);
        }

        dispatcher.setCallbackExecutor(environment.get().getCallbackExecutor());
    }

    protected PromiseEnvironment getEnvironment() {
        return environment.get();
    }

    public PromiseEvent getReasonOfTaskCompletion() {
        return reasonOfTaskCompletion;
    }

    public T getSuccessResult() {
        if (reasonOfTaskCompletion instanceof SuccessEvent) {
            return ((SuccessEvent<T>) reasonOfTaskCompletion).getData();
        }

        throw new IllegalStateException("Result can be retrieved only if promise was successfully completed");
    }

    public boolean isCompleted() {
        return isComplete.get();
    }

    public boolean isAborted() {
        return isComplete.get() && reasonOfTaskCompletion.getClass() == AbortEvent.class;
    }

    public boolean isSuccessed() {
        return isComplete.get() && reasonOfTaskCompletion.getClass() == SuccessEvent.class;
    }

    public boolean isFailed() {
        return isComplete.get() && reasonOfTaskCompletion.getClass() == FailEvent.class;
    }

    public Promise<T> setTag(Object value) {
        tag = value;
        return this;
    }

    public Object getTag() {
        return tag;
    }

    /**
     * You can use this method for reset and reuse promise,
     * without creating new instance
     * <p/>
     * Warning: This method not thread safe,
     * you must be sure that the Promise instance is no longer used.
     *
     * @param instance - Promise whose state is reset
     */
    public static void reset(Promise instance) {
        if (!instance.isComplete.compareAndSet(true, false)) {
            throw new IllegalStateException("Can't reset not finished task");
        }

        final TimerTask timerTask = (TimerTask) instance.timerTask.getAndSet(null);
        if (timerTask != null) {
            timerTask.cancel();
        }

        instance.reasonOfTaskCompletion = null;
        instance.tag = null;

        instance.dispatcher.removeEventListeners();
        instance.dispatcher.setCallbackExecutor(null);
        instance.environment.set(null);
    }
}