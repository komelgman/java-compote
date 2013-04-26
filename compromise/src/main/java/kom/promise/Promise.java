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
import kom.util.callback.Callback;

import java.util.concurrent.Future;

@SuppressWarnings("UnusedDeclaration")
public interface Promise<T> extends Future<T> {
    public boolean cancel(Object data);
    public boolean cancel();

    public Promise<T> onSuccess(Callback<SuccessEvent<T>> callback);
    public Promise<T> onFail(Callback<FailEvent> callback);
    public Promise<T> onUpdate(Callback<UpdateEvent> callback);
    public Promise<T> onCancel(Callback<CancelEvent> callback);
    public Promise<T> onAny(Callback<PromiseEvent> callback);

    public Promise<T> timeout(long msecs);
    public Promise<T> await();

    public T tryGet();
    public Object rawGet();

    public boolean isSuccessed();
    public boolean isFailed();

    public Promise<T> setTag(Object value);
    public Object getTag();
}