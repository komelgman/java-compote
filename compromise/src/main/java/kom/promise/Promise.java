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

import kom.promise.events.*;
import kom.promise.util.AsyncContext;
import kom.util.callback.Callback;

@SuppressWarnings("UnusedDeclaration")
public interface Promise<T> {
    public Promise<T> onSuccess(Callback<? super SuccessEvent<T>> callback);
    public Promise<T> onFail(Callback<? super FailEvent> callback);
    public Promise<T> onUpdate(Callback<? super UpdateEvent> callback);
    public Promise<T> onAbort(Callback<? super AbortEvent> callback);
    public Promise<T> onAny(Callback<? super PromiseEvent> callback);

    public boolean abort(Object data);

    public Promise<T> timeout(int msecs);
    public Promise<T> await();

    public PromiseEvent getReasonOfTaskCompletion();
    public T getSuccessResult();

    public boolean isCompleted();
    public boolean isAborted();
    public boolean isSuccessed();
    public boolean isFailed();

    public Promise<T> setTag(Object value);
    public Object getTag();
}