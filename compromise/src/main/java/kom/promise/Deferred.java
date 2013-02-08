package kom.promise;

import kom.promise.events.*;
import kom.util.callback.Callback;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class Deferred<T> {

    private final Promise<T> promise;

    public Deferred() {
        this(null, null);
    }

    public Deferred(PromiseEnvironment environment, Callback<HaltEvent> canceller) {
        if (environment == null) {
            environment = PromiseEnvironment.getDefaultEnvironment();
        }

        this.promise = environment.getPromisePool().getObject();

        if (canceller != null) {
            promise.halt(canceller);
        }
    }

    public boolean resolve(T data) {
        return promise.notifyAll(SuccessEvent.class, data, true);
    }

    public boolean reject(Object data) {
        return promise.notifyAll(FailEvent.class, data, true);
    }

    public boolean update(Object data) {
        return promise.notifyAll(ProgressEvent.class, data, false);
    }

    public Promise<T> getPromise() {
        return promise;
    }

    public static Promise<List<Promise>> parallel(final List<Promise> promises) {
        final AtomicInteger count = new AtomicInteger(promises.size());
        final Deferred<List<Promise>> deferred = new Deferred<List<Promise>>();
        final Promise<List<Promise>> result = deferred.getPromise();

        result.fail(new PromiseCanceller(promises, "One of parallel tasks has failed"));

        final Callback<SuccessEvent> successCallback = new Callback<SuccessEvent>() {
            @Override
            public void handle(SuccessEvent event) {
                if (count.decrementAndGet() == 0) {
                    deferred.resolve(promises);
                }
            }
        };

        for (final Promise promise : promises) {
            promise.success(successCallback).fail(new Callback<FailEvent>() {
                @Override
                public void handle(FailEvent event) {
                    deferred.reject(promise);
                }
            });
        }

        return result;
    }


    public static Promise<Promise> earlier(final List<Promise> promises) {
        final Deferred<Promise> deferred = new Deferred<Promise>();
        final Promise<Promise> result = deferred.getPromise();

        result.always(new PromiseCanceller(promises, "One of earlier tasks has finished"));

        for (final Promise promise : promises) {
            promise.success(new Callback<SuccessEvent>() {
                @Override
                public void handle(SuccessEvent event) {
                    deferred.resolve(promise);
                }
            }).fail(new Callback<FailEvent>() {
                @Override
                public void handle(FailEvent event) {
                    deferred.reject(promise);
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
                    // but it still not thread safe and can cancel finished task
                    promise.cancel(new IllegalStateException(message));
                }
            }
        }
    }
}