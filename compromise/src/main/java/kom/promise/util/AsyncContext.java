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
import kom.promise.events.PromiseEvent;
import kom.promise.impl.DeferredImpl;
import kom.util.callback.Callback;
import kom.util.callback.CallbackExecutor;
import kom.util.callback.RunnableCallbackExecutor;

import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("UnusedDeclaration")
public class AsyncContext {
    private static final Timer scheduler = new Timer(true);

    private final CallbackExecutor callbackExecutor;
    private final ExecutorService runnableExecutor;

    public AsyncContext(ExecutorService runnableExecutor, CallbackExecutor callbackExecutor) {
        if (runnableExecutor == null) {
            runnableExecutor = Executors.newCachedThreadPool();
        }

        if (callbackExecutor == null) {
            callbackExecutor = RunnableCallbackExecutor.getInstance();
            callbackExecutor.setRunnableExecutor(runnableExecutor);
        }

        this.runnableExecutor = runnableExecutor;
        this.callbackExecutor = callbackExecutor;
    }

    public <T> void executeCallback(Callback<T> callback, T data) {
        if (callbackExecutor == null) {
            callback.handle(data);
        } else {
            callbackExecutor.execute(callback, data);
        }
    }

    public void executeRunnable(Runnable runnable) {
        runnableExecutor.execute(runnable);
    }

    public <T extends PromiseEvent> T event(Class<T> reasonType) {
        try {
            // can use object pool in this point
            return reasonType.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Can't create new instance for Event object", e);
        }
    }

    public <T> Deferred<T> deferred() {
        // can use object pool in this point
        return new DeferredImpl<T>(this);
    }

    public Timer getScheduler() {
        return scheduler;
    }

    public CallbackExecutor getCallbackExecutor() {
        return callbackExecutor;
    }

    public ExecutorService getRunnableExecutor() {
        return runnableExecutor;
    }
}