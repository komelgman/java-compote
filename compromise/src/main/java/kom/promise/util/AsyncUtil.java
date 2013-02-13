package kom.promise.util;

import kom.promise.Deferred;
import kom.promise.Promise;
import kom.promise.events.AbortEvent;
import kom.promise.events.FailEvent;
import kom.promise.events.PromiseEvent;
import kom.promise.events.SuccessEvent;
import kom.util.callback.Callback;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class AsyncUtil {

    public static Promise<List<AsyncTask>> chain(AsyncTask ... tasks) {
        return chain(null, asList(tasks));
    }

    public static Promise<List<AsyncTask>> chain(List<AsyncTask> tasks) {
        return chain(null, tasks);
    }

    public static Promise<List<AsyncTask>> chain(PromiseEnvironment environment, AsyncTask ... tasks) {
        return chain(environment, asList(tasks));
    }

    public static Promise<List<AsyncTask>> chain(PromiseEnvironment environment, final List<AsyncTask> tasks) {
        if (environment == null) {
            environment = PromiseEnvironment.getDefaultEnvironment();
        }

        final Promise<List<AsyncTask>> result = environment.getPromise();
        final Deferred<List<AsyncTask>> deferred = new Deferred<List<AsyncTask>>(result);

        environment.executeRunnable(new Runnable() {
            private AtomicBoolean isCancelled = new AtomicBoolean(false);
            private AtomicReference<AsyncTask> currentTask;

            @Override
            public void run() {
                result.onAbort(new Callback<AbortEvent>() {
                    @Override
                    public void handle(AbortEvent message) {
                        isCancelled.set(true);
                        currentTask.get().abort(message);
                    }
                });

                for (final AsyncTask task : tasks) {
                    currentTask.set(task);

                    task.start().await();

                    Class reason = task.getReasonOfTaskCompletion().getClass();
                    if (reason.equals(FailEvent.class) || reason.equals(AbortEvent.class)) {
                        deferred.reject(task);
                        return;
                    }

                    if (isCancelled.get()) {
                        return;
                    }

                    deferred.update(task);
                }

                deferred.resolve(tasks);
            }
        });

        return result;
    }

    public static Promise<List<Promise>> parallel(Promise ... promises) {
        return parallel(null, asList(promises));
    }

    public static Promise<List<Promise>> parallel(List<Promise> promises) {
        return parallel(null, promises);
    }

    public static Promise<List<Promise>> parallel(PromiseEnvironment environment, Promise ... promises) {
        return parallel(environment, asList(promises));
    }

    public static Promise<List<Promise>> parallel(PromiseEnvironment environment, final List<Promise> promises) {
        if (environment == null) {
            environment = PromiseEnvironment.getDefaultEnvironment();
        }

        final AtomicInteger count = new AtomicInteger(promises.size());
        final Promise<List<Promise>> result = environment.getPromise();
        final Deferred<List<Promise>> deferred = new Deferred<List<Promise>>(result);

        result.onFail(new PromiseCanceller(promises, "One of parallel tasks was failed"))
              .onAbort(new PromiseCanceller(promises, "One of parallel tasks (or main parallel task) was cancelled"));

        for (final Promise promise : promises) {
            promise.onSuccess(new Callback<PromiseEvent>() {
                        @Override
                        public void handle(PromiseEvent event) {
                            if (count.decrementAndGet() == 0) {
                                deferred.resolve(promises);
                            } else {
                                deferred.update(promise);
                            }
                        }
                    })
                    .onAbort(new Callback<AbortEvent>() {
                        @Override
                        public void handle(AbortEvent message) {
                            if (!result.isCompleted())
                                result.abort(promise);
                        }
                    })
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

    public static Promise<Promise> earlier(List<Promise> promises) {
        return earlier(null, promises);
    }

    public static Promise<Promise> earlier(PromiseEnvironment environment, Promise ... promises) {
        return earlier(environment, asList(promises));
    }

    public static Promise<Promise> earlier(PromiseEnvironment environment, final List<Promise> promises) {
        if (environment == null) {
            environment = PromiseEnvironment.getDefaultEnvironment();
        }

        final Promise<Promise> result = environment.getPromise();
        final Deferred<Promise> deferred = new Deferred<Promise>(result);

        PromiseCanceller doneCanceller = new PromiseCanceller(promises, "One of earlier tasks was finished");
        PromiseCanceller abortCanceller = new PromiseCanceller(promises,
                "One of earlier tasks (or main earlier promise) was cancelled");

        result.onFail(doneCanceller)
              .onSuccess(doneCanceller)
              .onAbort(abortCanceller);

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
                            if (!result.isCompleted())
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
                if (!promise.isCompleted()) { // skip already finished,
                    // but it still not thread safe and can try stopping finished task
                    promise.abort(new IllegalStateException(message));
                }
            }
        }
    }
}