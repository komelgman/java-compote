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

package kom.promise.util;

import kom.promise.Deferred;
import kom.promise.Promise;
import kom.promise.events.PromiseEvent;
import kom.promise.events.AbortEvent;
import kom.promise.events.FailEvent;
import kom.promise.events.SuccessEvent;
import kom.promise.exceptions.PromiseException;
import kom.util.callback.Callback;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;

// todo: change implementation of body to AsyncTask

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class AsyncUtil {

    public static <T> Promise<T> wrap(Future<T> future) {
        return wrap(null, future);
    }

    public static <T> Promise<T> wrap(AsyncContext environment, final Future<T> future) {
        return null;//wrap(null, future); todo!!!
    }


    public static <T> Promise<T> wrap(Callable<T> callable) {
        return wrap(null, callable);
    }

    public static <T> Promise<T> wrap(AsyncContext environment, final Callable<T> callable) {
        if (environment == null) {
            environment = AsyncContext.defaultContext();
        }

        final Deferred<T> deferred = environment.deferred();

        environment.executeRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    deferred.resolve(callable.call());
                } catch (Exception e) {
                    deferred.reject(e);
                }
            }
        });

        return deferred.getPromise();
    }

    public static Promise<List<AsyncTask>> chain(AsyncTask... tasks) {
        return chain(null, asList(tasks));
    }

    public static Promise<List<AsyncTask>> chain(List<AsyncTask> tasks) {
        return chain(null, tasks);
    }

    public static Promise<List<AsyncTask>> chain(AsyncContext environment, AsyncTask... tasks) {
        return chain(environment, asList(tasks));
    }

    public static Promise<List<AsyncTask>> chain(AsyncContext environment, final List<AsyncTask> tasks) {
        if (environment == null) {
            environment = AsyncContext.defaultContext();
        }

        final Deferred<List<AsyncTask>> deferred = environment.deferred();

        environment.executeRunnable(new Runnable() {
            private AtomicBoolean isCancelled = new AtomicBoolean(false);
            private AtomicReference<AsyncTask> currentTask = new AtomicReference<AsyncTask>();

            @Override
            public void run() {
                deferred.onAbort(new Callback<AbortEvent>() {
                    @Override
                    public void handle(AbortEvent message) {
                        isCancelled.set(true);
                        currentTask.get().abort(message);
                    }
                });

                for (final AsyncTask task : tasks) {
                    currentTask.set(task);

                    task.start().await();
                    if (!task.isSuccessed()) {
                        deferred.reject(task);
                    }

                    if (isCancelled.get()) {
                        return;
                    }

                    deferred.update(task);
                }

                deferred.resolve(tasks);
            }
        });

        return deferred.getPromise();
    }

    public static Promise<List<Promise>> parallel(Promise... promises) {
        return parallel(null, asList(promises));
    }

    public static Promise<List<Promise>> parallel(List<Promise> promises) {
        return parallel(null, promises);
    }

    public static Promise<List<Promise>> parallel(AsyncContext environment, Promise... promises) {
        return parallel(environment, asList(promises));
    }

    public static Promise<List<Promise>> parallel(AsyncContext environment, final List<Promise> promises) {
        if (environment == null) {
            environment = AsyncContext.defaultContext();
        }

        final AtomicInteger count = new AtomicInteger(promises.size());
        final Deferred<List<Promise>> deferred = environment.deferred();

        deferred.onFail(new PromiseCanceller(promises, "One of parallel tasks was failed"))
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
                            if (!deferred.isCompleted())
                                deferred.abort(promise);
                        }
                    })
                    .onFail(new Callback<FailEvent>() {
                        @Override
                        public void handle(FailEvent event) {
                            deferred.reject(promise);
                        }
                    });
        }

        return deferred.getPromise();
    }

    public static Promise<Promise> earlier(Promise... promises) {
        return earlier(null, asList(promises));
    }

    public static Promise<Promise> earlier(List<Promise> promises) {
        return earlier(null, promises);
    }

    public static Promise<Promise> earlier(AsyncContext environment, Promise... promises) {
        return earlier(environment, asList(promises));
    }

    public static Promise<Promise> earlier(AsyncContext environment, final List<Promise> promises) {
        if (environment == null) {
            environment = AsyncContext.defaultContext();
        }

        final Deferred<Promise> deferred = environment.deferred();

        final PromiseCanceller doneCanceller = new PromiseCanceller(promises, "One of earlier tasks was finished");
        final PromiseCanceller abortCanceller = new PromiseCanceller(promises,
                "One of earlier tasks (or main earlier promise) was cancelled");

        deferred.onFail(doneCanceller)
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
                            if (!deferred.isCompleted())
                                deferred.abort(promise);
                        }
                    });
        }

        return deferred.getPromise();
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