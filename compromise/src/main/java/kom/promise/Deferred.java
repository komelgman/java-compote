package kom.promise;

import kom.util.Callback;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Deferred<T> {

    private final Promise<T> promise;

    public Deferred() {
        this(null);
    }

    public Deferred(Callback canceller) {
        promise = PromiseFactory.getPromise();

        if (canceller != null) {
            promise.halt(canceller);
        }
    }

    public boolean resolve(T data) {
        return promise.notifyAll(PromiseEventFactory.getSuccessEvent(data), true);
    }

    public boolean reject(Object data) {
        return promise.notifyAll(PromiseEventFactory.getFailEvent(data), true);
    }

    public boolean update(Object data) {
        return promise.notifyAll(PromiseEventFactory.getProgressEvent(data), false);
    }

    public Promise<T> getPromise() {
        return promise;
    }

    @SuppressWarnings("unchecked")
    public static Promise<List<Promise>> parallel(final List<Promise> promises) {
        final AtomicInteger count = new AtomicInteger(promises.size());
        final Deferred<List<Promise>> deferred = new Deferred<List<Promise>>();

        final Callback successCallback = new Callback() {
            @Override
            public void handle(Object data) {
                if (count.decrementAndGet() == 0) {
                    deferred.resolve(promises);
                }
            }
        };

        // cancel runned tasks if one failed
        deferred.getPromise().fail(new Callback() {
            @Override
            public void handle(Object data) {
                for (Promise promise : promises) {
                    if (!promise.isFinished()) { // skip already finished,
                        // but it still not thread safe and can cancel finished task
                        promise.cancel(new IllegalStateException("One of parallel tasks has failed"));
                    }
                }
            }
        });

        for (final Promise promise : promises) {
            promise.success(successCallback).fail(new Callback() {
                @Override
                public void handle(Object data) {
                    deferred.reject(promise);
                }
            });
        }

        return deferred.getPromise();
    }


    @SuppressWarnings("unchecked")
    public static Promise<Promise> earlier(final List<Promise> promises) {
        final Deferred<Promise> deferred = new Deferred<Promise>();

        deferred.getPromise().always(new Callback() {
            @Override
            public void handle(Object data) {
                for (Promise promise : promises) {
                    if (!promise.isFinished()) { // skip already finished,
                        // but it still not thread safe and can cancel finished task
                        promise.cancel(new IllegalStateException("One of earlier tasks has finished"));
                    }
                }
            }
        });

        for (final Promise promise : promises) {
            promise.success(new Callback() {
                @Override
                public void handle(Object data) {
                    deferred.resolve(promise);
                }
            }).fail(new Callback() {
                @Override
                public void handle(Object data) {
                    deferred.reject(promise);
                }
            });
        }

        return deferred.getPromise();
    }
}