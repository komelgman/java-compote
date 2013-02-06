package kom.promise;

import kom.promise.events.FailEvent;
import kom.promise.events.ProgressEvent;
import kom.promise.events.SuccessEvent;
import kom.util.Callback;
import kom.util.SimpleObjectPool;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class Deferred<T> {

    private static final SimpleObjectPool<Promise> promisePool = new SimpleObjectPool<Promise>(128, Promise.class);
    private final Promise<T> promise;

    public Deferred() {
        this(promisePool.getObject(), null);
    }

    public Deferred(Promise promise) {
        this(promise, null);
    }

    public Deferred(Promise<T> promise, Callback canceller) {
        if (promise == null) {
            throw new NullPointerException("Promise can't be null");
        }

        this.promise = promise;

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