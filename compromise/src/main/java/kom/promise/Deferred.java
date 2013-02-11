package kom.promise;

import kom.promise.events.*;
import kom.promise.util.PromiseEnvironment;
import kom.util.callback.Callback;

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class Deferred<T> {

    private final Promise<T> promise;

    public Deferred() {
        this((PromiseEnvironment)null, null);
    }

    public Deferred(PromiseEnvironment environment) {
        this(environment, null);
    }

    public Deferred(Promise promise) {
        this(promise, null);
    }

    public Deferred(PromiseEnvironment environment, Callback<AbortEvent> canceller) {
        if (environment == null) {
            environment = PromiseEnvironment.getDefaultEnvironment();
        }

        promise = environment.getPromise();

        if (canceller != null) {
            promise.onAbort(canceller);
        }
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
        return promise.notifyAll(SuccessEvent.class, data, true);
    }

    public boolean reject(Object data) {
        return promise.notifyAll(FailEvent.class, data, true);
    }

    public boolean update(Object data) {
        return promise.notifyAll(UpdateEvent.class, data, false);
    }

    public Promise<T> getPromise() {
        return promise;
    }
}