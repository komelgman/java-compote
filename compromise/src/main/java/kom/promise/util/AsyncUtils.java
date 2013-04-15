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
import kom.promise.events.CancelEvent;
import kom.promise.events.FailEvent;
import kom.promise.events.PromiseEvent;
import kom.promise.events.SuccessEvent;
import kom.util.callback.Callback;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;

// todo: change implementation of body to AsyncTask

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class AsyncUtils {

    public static <T> Promise<T> wrap(Future<T> future) {
        return wrap(null, future);
    }

    public static <T> Promise<T> wrap(AsyncContext context, final Future<T> future) {
        if (context == null) {
            context = defaultContext();
        }

        final Deferred<T> deferred = context.deferred();

        deferred.onCancel(new Callback<CancelEvent>() {
            @Override
            public void handle(CancelEvent message) {
                if (message.getData() instanceof Boolean) {
                    future.cancel((Boolean)message.getData());    
                } else {
                    future.cancel(false);
                }                
            }
        });
        
        context.executeRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    deferred.resolve(future.get());
                } catch (Exception e) {
                    deferred.reject(e);
                }
            }
        });

        return deferred.getPromise();
    }


    public static <T> Promise<T> wrap(Callable<T> callable) {
        return wrap(null, callable);
    }

    public static <T> Promise<T> wrap(AsyncContext context, final Callable<T> callable) {
        if (context == null) {
            context = defaultContext();
        }

        return wrap(context, context.getRunnableExecutor().submit(callable));
    }

    public static Promise<List<AsyncTask>> chain(AsyncTask... tasks) {
        return chain(null, asList(tasks));
    }

    public static Promise<List<AsyncTask>> chain(List<AsyncTask> tasks) {
        return chain(null, tasks);
    }

    public static Promise<List<AsyncTask>> chain(AsyncContext context, AsyncTask... tasks) {
        return chain(context, asList(tasks));
    }

    public static Promise<List<AsyncTask>> chain(AsyncContext context, final List<AsyncTask> tasks) {
        if (context == null) {
            context = defaultContext();
        }

        final Deferred<List<AsyncTask>> deferred = context.deferred();

        context.executeRunnable(new Runnable() {
            private AtomicBoolean isCancelled = new AtomicBoolean(false);
            private AtomicReference<AsyncTask> currentTask = new AtomicReference<AsyncTask>();

            @Override
            public void run() {
                deferred.onCancel(new Callback<CancelEvent>() {
                    @Override
                    public void handle(CancelEvent message) {
                        isCancelled.set(true);
                        currentTask.get().cancel(message);
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

    public static Promise<List<Promise>> parallel(AsyncContext context, Promise... promises) {
        return parallel(context, asList(promises));
    }

    public static Promise<List<Promise>> parallel(AsyncContext context, final List<Promise> promises) {
        if (context == null) {
            context = defaultContext();
        }

        final AtomicInteger count = new AtomicInteger(promises.size());
        final Deferred<List<Promise>> deferred = context.deferred();

        deferred.onFail(new PromiseCanceller(promises, "One of parallel tasks was failed"))
                .onCancel(new PromiseCanceller(promises, "One of parallel tasks (or main parallel task) was cancelled"));

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
                    .onCancel(new Callback<CancelEvent>() {
                        @Override
                        public void handle(CancelEvent message) {
                            if (!deferred.isDone())
                                deferred.cancel(promise);
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

    public static Promise<Promise> earlier(AsyncContext context, Promise... promises) {
        return earlier(context, asList(promises));
    }

    public static Promise<Promise> earlier(AsyncContext context, final List<Promise> promises) {
        if (context == null) {
            context = defaultContext();
        }

        final Deferred<Promise> deferred = context.deferred();

        final PromiseCanceller doneCanceller = new PromiseCanceller(promises, "One of earlier tasks was finished");
        final PromiseCanceller abortCanceller = new PromiseCanceller(promises,
                "One of earlier tasks (or main earlier promise) was cancelled");

        deferred.onFail(doneCanceller)
                .onSuccess(doneCanceller)
                .onCancel(abortCanceller);

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
                    .onCancel(new Callback<CancelEvent>() {
                        @Override
                        public void handle(CancelEvent event) {
                            if (!deferred.isDone())
                                deferred.cancel(promise);
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
                if (!promise.isDone()) { // skip already finished,
                    // but it still not thread safe and can try stopping finished task
                    promise.cancel(new IllegalStateException(message));
                }
            }
        }
    }

    public static AsyncContext defaultContext() {
        return AsyncContextHolder.HOLDER_INSTANCE;
    }

    private static class AsyncContextHolder {
        public static final AsyncContext HOLDER_INSTANCE = new AsyncContext(null, null);
    }
}