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

import kom.promise.events.CancelEvent;
import kom.promise.events.SuccessEvent;
import kom.promise.events.UpdateEvent;
import kom.promise.util.AsyncTask;
import kom.util.callback.Callback;

import java.util.List;

import static kom.promise.util.AsyncUtils.chain;

public class ChainUsage {

    private String someContext = "";

    private void example1() {
        System.out.println("Example 1: Chained tasks and some common context");

        chain(
                createAsyncTask(3000, "async task 1"),
                createAsyncTask(3000, "async task 2"),
                createAsyncTask(3000, "async task 3")
        ).onUpdate(new Callback<UpdateEvent>() {
            @Override
            public void handle(UpdateEvent message) {
                Object data = message.getData();
                if (data instanceof Promise) {
                    System.out.println(((Promise) data).getTag()
                            + " -> " + ((Promise) data).tryGet());
                }
            }
        }).onSuccess(new Callback<SuccessEvent<List<AsyncTask>>>() {
            @Override
            public void handle(SuccessEvent<List<AsyncTask>> message) {
                System.out.println(someContext);
            }
        });
    }

    private AsyncTask<String> createAsyncTask(final int delay, final String tag) {
        return new AsyncTask<String>() {
            private volatile boolean isCancelled = false;

            {
                setTag(tag);
            }

            @Override
            public void run() {
                try {
                    // some long job
                    int i = 0;
                    while (i++ < delay / 10 && !isCancelled) {
                        update(i);
                        Thread.sleep(10);
                    }

                    // if not stopped return result
                    if (!isCancelled) {
                        someContext += getTag() + "; ";
                        resolve("successfully resolved");
                    }
                } catch (InterruptedException e) {
                    // if something wrong return error
                    reject(e);
                }
            }

            @Override
            public void handle(CancelEvent message) {
                isCancelled = true;
            }
        };
    }

    public static void main(String[] args) {
        System.out.println("Program not closed immediately because used cached thread pool");

        ChainUsage usage = new ChainUsage();
        usage.example1();
    }
}
