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

import kom.promise.events.SuccessEvent;
import kom.promise.util.AsyncTask;
import kom.util.callback.Callback;

/**
 * User: syungman
 * Date: 11.02.13
 */
public class AsyncTaskUsage {

    public static void main(String[] args) {
        System.out.println("Program not closed immediately because used cached thread pool");

        AsyncTaskUsage usage = new AsyncTaskUsage();
        usage.example1();
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


    private Promise<String> someLongMethod(final int delay) {
        return new AsyncTask<String>() {
            {
                // setEnvironment(?); // can set some environment here
            }

            private volatile boolean isCancelled = false;

            @Override
            protected void process() {
                try {
                    // some long job
                    int i = 0;
                    while (i++ < delay / 10 && !isCancelled) {
                        update(i);
                        Thread.sleep(10);
                    }

                    // if not stopped return result
                    if (!isCancelled) {
                        resolve("successfully resolved");
                    }
                } catch (InterruptedException e) {
                    // if something wrong return error
                    reject(e);
                }
            }

            @Override
            protected void canceller() {
                isCancelled = true;
            }
        }.start();
    }
}
