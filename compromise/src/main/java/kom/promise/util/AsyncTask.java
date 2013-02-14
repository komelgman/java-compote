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
import kom.promise.events.AbortEvent;
import kom.util.callback.Callback;

@SuppressWarnings("UnusedDeclaration")
public abstract class AsyncTask<T> extends Promise<T> {

    private final Deferred<T> deferred;

    public AsyncTask() {
        this.deferred = new Deferred<T>(this, new TaskCanceller());
    }

    public final AsyncTask<T> start() {
        getEnvironment().executeRunnable(new TaskProcessor());

        return this;
    }

    protected final void resolve(T data) {
        deferred.resolve(data);
    }

    protected final void reject(Object data) {
        deferred.reject(data);
    }

    protected final void update(Object data) {
        deferred.update(data);
    }

    protected abstract void process();

    protected /* virtual */ void canceller() {
    }


    private class TaskProcessor implements Runnable {
        @Override
        public void run() {
            AsyncTask.this.process();
        }
    }

    private class TaskCanceller implements Callback<AbortEvent> {
        @Override
        public void handle(AbortEvent message) {
            AsyncTask.this.canceller();
        }
    }
}