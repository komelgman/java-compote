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

package kom.promise.exceptions;

import kom.promise.events.PromiseEvent;

public class PromiseException extends Exception {

    private final Class<? extends PromiseEvent> reason;
    private final Object data;

    public PromiseException(Class<? extends PromiseEvent> reason, Object data) {
        this.reason = reason;

        this.data = data;
    }

    public Class<? extends PromiseEvent> getReason() {
        return reason;
    }

    public Object getData() {
        return data;
    }
}
