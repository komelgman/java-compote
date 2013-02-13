package kom.promise;

import kom.promise.events.*;
import kom.promise.util.PromiseEnvironment;
import kom.util.callback.Callback;

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class Deferred<T> {

    private final Promise<T> promise;

    public Deferred() {
        this(PromiseEnvironment.getDefaultEnvironment().getPromise(), null);
    }

    public Deferred(Promise promise) {
        this(promise, null);
    }

    public Deferred(Promise promise, Callback<AbortEvent> canceller) {
        if (promise == null) {
            throw new NullPointerException("Promise can't be NULL");
        }

        this.promise = promise;

        if (canceller != null) {
            promise.onAbort(canceller);
        }
    }

    public boolean resolve(T data) {
        return promise.signalAboutCompletion(SuccessEvent.class, data);
    }

    public boolean reject(Object data) {
        return promise.signalAboutCompletion(FailEvent.class, data);
    }

    public boolean update(Object data) {
        return promise.signalAboutProgress(UpdateEvent.class, data);
    }

    public Promise<T> getPromise() {
        return promise;
    }
}