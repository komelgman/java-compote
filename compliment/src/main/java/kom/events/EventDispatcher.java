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

package kom.events;


import kom.util.callback.Callback;
import kom.util.callback.CallbackExecutor;

public interface EventDispatcher<T extends Event> {
    public <Y extends T> void addEventListener(Class<Y> eventType, Callback<? super Y> listener);

    public void removeEventListeners();

    public void removeEventListeners(Class<? extends T> eventType);

    public <Y extends T> void removeEventListener(Class<Y> eventType, Callback<? super Y> listener);

    public void dispatchEvent(T event);

    public void setCallbackExecutor(CallbackExecutor executor);
}
