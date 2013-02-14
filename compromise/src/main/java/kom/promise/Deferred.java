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

package kom.promise;

import kom.promise.events.AbortEvent;
import kom.promise.events.FailEvent;
import kom.promise.events.SuccessEvent;
import kom.promise.events.UpdateEvent;
import kom.promise.util.PromiseEnvironment;
import kom.util.callback.Callback;

@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public class Deferred<T> {

    private final Promise<T> promise;

    public Deferred() {
        this(PromiseEnvironment.getDefaultEnvironment().getPromise(), null);
    }

    public Deferred(Promise promise) {
        this(promise, null);
    }

    public Deferred(Promise promise, Callback<AbortEvent> canceller) {
        if (promise == null) {
            throw new NullPointerException("Promise can't be NULL");
        }

        this.promise = promise;

        if (canceller != null) {
            promise.onAbort(canceller);
        }
    }

    public boolean resolve(T data) {
        return promise.signalAboutCompletion(SuccessEvent.class, data);
    }

    public boolean reject(Object data) {
        return promise.signalAboutCompletion(FailEvent.class, data);
    }

    public boolean update(Object data) {
        return promise.signalAboutProgress(UpdateEvent.class, data);
    }

    public Promise<T> getPromise() {
        return promise;
    }
}