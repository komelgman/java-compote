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

import kom.promise.events.CancelEvent;
import kom.promise.events.FailEvent;
import kom.promise.events.SuccessEvent;
import kom.promise.events.UpdateEvent;
import kom.promise.impl.PromiseImpl;
import kom.util.callback.Callback;

@SuppressWarnings("UnusedDeclaration")
public abstract class AsyncTask<T> extends PromiseImpl<T> implements Callback<CancelEvent>, Runnable {

    public AsyncTask() {
        onCancel(this);
    }

    public final AsyncTask<T> start() {
        context().executeRunnable(this);

        return this;
    }

    protected final void resolve(T data) {
        triggerEventAndStopProcessing(SuccessEvent.class, data);
    }

    protected final void reject(Object data) {
        triggerEventAndStopProcessing(FailEvent.class, data);
    }

    protected final void update(Object data) {
        triggerEvent(UpdateEvent.class, data);
    }

    @Override
    public void handle(CancelEvent message) {
        // virtual
    }
}