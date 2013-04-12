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

package kom.promise.impl;

import kom.promise.Deferred;
import kom.promise.Promise;
import kom.promise.events.FailEvent;
import kom.promise.events.SuccessEvent;
import kom.promise.events.UpdateEvent;
import kom.promise.util.AsyncContext;

public class DeferredImpl<T> extends PromiseImpl<T> implements Deferred<T> {

    public DeferredImpl(AsyncContext context) {
        setContext(context);
    }

    @Override
    public boolean resolve(T data) {
        return triggerEventAndStopProcessing(SuccessEvent.class, data);
    }

    @Override
    public boolean reject(Object data) {
        return triggerEventAndStopProcessing(FailEvent.class, data);
    }

    @Override
    public boolean update(Object data) {
        return triggerEvent(UpdateEvent.class, data);
    }

    @Override
    public Promise<T> getPromise() {
        return this;
    }


    //
//    /**
//     * You can use this method for reset and reuse promise,
//     * without creating new instance
//     * <p/>
//     * Warning: This method not thread safe,
//     * you must be sure that the Promise instance is no longer used.
//     *
//     * @param instance - Promise whose state is reset
//     */
//    public static void reset(Promise instance) {
//        if (!instance.isComplete.compareAndSet(true, false)) {
//            throw new IllegalStateException("Can't reset not finished task");
//        }
//
//        final TimerTask timerTask = (TimerTask) instance.timerTask.getAndSet(null);
//        if (timerTask != null) {
//            timerTask.cancel();
//        }
//
//        instance.reasonOfTaskCompletion = null;
//        instance.tag = null;
//
//        instance.dispatcher.removeEventListeners();
//        instance.dispatcher.setCallbackExecutor(null);
//        instance.environment.set(null);
//    }
}
