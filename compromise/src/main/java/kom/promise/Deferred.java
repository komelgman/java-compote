package kom.promise;

import kom.promise.events.*;
import kom.util.callback.Callback;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class Deferred<T> {

    private final Promise<T> promise;

    public Deferred() {
        this(null, null);
    }

    public Deferred(PromiseEnvironment environment, Callback<AbortEvent> canceller) {
        if (environment == null) {
            environment = PromiseEnvironment.getDefaultEnvironment();
        }

        this.promise = environment.getPromisePool().getObject();

        if (canceller != null) {
            promise.onAbort(canceller);
        }
    }

    public Deferred(PromiseEnvironment environment) {
        this(environment, null);
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

    public static Promise<List<Promise>> parallel(Promise ... promises) {
        return parallel(null, asList(promises));
    }

    public static Promise<List<Promise>> parallel(final List<Promise> promises) {
        return parallel(null, promises);
    }

    public static Promise<List<Promise>> parallel(PromiseEnvironment environment, Promise ... promises) {
        return parallel(environment, asList(promises));
    }

    public static Promise<List<Promise>> parallel(PromiseEnvironment environment, final List<Promise> promises) {
        final AtomicInteger count = new AtomicInteger(promises.size());
        final Deferred<List<Promise>> deferred = new Deferred<List<Promise>>(environment);
        final Promise<List<Promise>> result = deferred.getPromise();

        result.onFail(new PromiseCanceller(promises, "One of parallel tasks has failed"))
                .onAbort(new PromiseCanceller(promises, "Parallel tasks was cancelled"));

        final Callback<PromiseEvent> successCallback = new Callback<PromiseEvent>() {
            @Override
            public void handle(PromiseEvent event) {
                if (count.decrementAndGet() == 0) {
                    deferred.resolve(promises);
                }
            }
        };

        for (final Promise promise : promises) {
            promise.onSuccess(successCallback)
                    .onAbort(successCallback)
                    .onFail(new Callback<FailEvent>() {
                        @Override
                        public void handle(FailEvent event) {
                            deferred.reject(promise);
                        }
                    });
        }

        return result;
    }

    public static Promise<Promise> earlier(Promise ... promises) {
        return earlier(null, asList(promises));
    }

    public static Promise<Promise> earlier(final List<Promise> promises) {
        return earlier(null, promises);
    }

    public static Promise<Promise> earlier(PromiseEnvironment environment, Promise ... promises) {
        return earlier(environment, asList(promises));
    }

    public static Promise<Promise> earlier(PromiseEnvironment environment, final List<Promise> promises) {
        final Deferred<Promise> deferred = new Deferred<Promise>(environment);
        final Promise<Promise> result = deferred.getPromise();

        result.onAny(new PromiseCanceller(promises, "One of earlier tasks has finished"));

        for (final Promise promise : promises) {
            promise.onSuccess(new Callback<SuccessEvent>() {
                @Override
                public void handle(SuccessEvent event) {
                    deferred.resolve(promise);
                }
            })
                    .onFail(new Callback<FailEvent>() {
                        @Override
                        public void handle(FailEvent event) {
                            deferred.reject(promise);
                        }
                    })
                    .onAbort(new Callback<AbortEvent>() {
                        @Override
                        public void handle(AbortEvent event) {
                            result.abort(promise);
                        }
                    });
        }

        return result;
    }


    private static class PromiseCanceller implements Callback {
        private final List<Promise> promises;
        private final String message;

        public PromiseCanceller(List<Promise> promises, String message) {
            this.promises = promises;
            this.message = message;
        }

        @Override
        public void handle(Object event) {
            for (Promise promise : promises) {
                if (!promise.isFinished()) { // skip already finished,
                    // but it still not thread safe and can try stopping finished task
                    promise.abort(new IllegalStateException(message));
                }
            }
        }
    }
}