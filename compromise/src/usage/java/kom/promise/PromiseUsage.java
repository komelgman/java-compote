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
import kom.promise.events.SuccessEvent;
import kom.promise.events.UpdateEvent;
import kom.util.callback.Callback;

public class PromiseUsage {

    public static void main(String[] args) {
        PromiseUsage usage = new PromiseUsage();
        usage.example1();
        usage.example2();
        usage.example3();
        usage.example4();
    }

    private void example1() {
        System.out.println("Example 1: simple example, please wait 3 sec");

        Promise<String> promise = someLongMethod(5000);
        promise.onSuccess(new Callback<SuccessEvent<String>>() {
            @Override
            public void handle(SuccessEvent<String> message) {
                System.out.println("Example 1: " + message.getData());
            }
        });
    }

    private void example2() {
        System.out.println("Example 2: timeout example, please wait 2 sec");

        Promise<String> promise = someLongMethod(5000);
        promise.onSuccess(new Callback<SuccessEvent<String>>() {
            @Override
            public void handle(SuccessEvent<String> message) {
                System.out.println("Example 2 success: " + message.getData());
            }
        }).onAbort(new Callback<AbortEvent>() {
            @Override
            public void handle(AbortEvent message) {
                System.out.println("Example 2 onAbort: " + message.getData().toString());
            }
        }).timeout(2000);
    }

    private void example3() {
        System.out.println("Example 3: blocking semantic, please wait 10 sec");

        String result = someLongMethod(10000).await().getSuccessResult();
        System.out.println("Example 3 result: " + result);
    }

    private void example4() {
        System.out.println("Example 4: update state, please wait 1 sec");

        String result = someLongMethod(1000).onUpdate(new Callback<UpdateEvent>() {
            @Override
            public void handle(UpdateEvent message) {
                System.out.println("Example 4 progress: " + message.getData());
            }
        }).await().getSuccessResult();
        System.out.println("Example 4 result: " + result);
    }


    private Promise<String> someLongMethod(final int delay) {
        final Deferred<String> deferred = new Deferred<String>();

        new Thread() {
            private boolean isCancelled = false;

            @Override
            public void run() {
                // job stopper
                deferred.getPromise().onAbort(new Callback<AbortEvent>() {
                    @Override
                    public void handle(AbortEvent message) {
                        isCancelled = true;
                    }
                });

                try {
                    // some long job
                    int i = 0;
                    while (i++ < delay / 10 && !isCancelled) {
                        deferred.update(i);
                        sleep(10);
                    }

                    // if not stopped return result
                    if (!isCancelled) {
                        deferred.resolve("successfully resolved");
                    }
                } catch (InterruptedException e) {
                    // if something wrong return error
                    deferred.reject(e);
                }
            }
        }.start();

        return deferred.getPromise();
    }
}