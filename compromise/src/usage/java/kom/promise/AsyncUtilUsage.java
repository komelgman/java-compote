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
import kom.promise.util.AsyncTask;
import kom.util.callback.Callback;

import java.util.List;

import static kom.promise.util.AsyncUtil.earlier;
import static kom.promise.util.AsyncUtil.parallel;

public class AsyncUtilUsage {

    public static void main(String[] args) {
        System.out.println("Program not closed immediately because used cached thread pool");

        AsyncUtilUsage usage = new AsyncUtilUsage();
        usage.example1();
        usage.example2();
        usage.example3();
    }

    private void example1() {
        System.out.println("Example 1: Parallel task execution");

        parallel(
                someLongOperation(4000).setTag("Example 1/Operation 1"),
                someLongOperation(2500).setTag("Example 1/Operation 2"),
                someLongOperation(3200).setTag("Example 1/Operation 3")
        ).onSuccess(new Callback<SuccessEvent<List<Promise>>>() {
            @Override
            public void handle(SuccessEvent<List<Promise>> message) {
                for (Promise promise : message.getData()) {
                    System.out.println(promise.getTag() + " -> " + promise.getSuccessResult());
                }
            }
        });
    }

    private void example2() {
        System.out.println("Example 2: Earlier task execution");

        earlier(
                someLongOperation(3000).setTag("Example 2/Operation 1"),
                someLongOperation(2000).setTag("Example 2/Operation 2"),
                someLongOperation(3200).setTag("Example 2/Operation 3")
        ).onSuccess(new Callback<SuccessEvent<Promise>>() {
            @Override
            public void handle(SuccessEvent<Promise> message) {
                Promise promise = message.getData();
                System.out.println(promise.getTag() + " -> " + promise.getSuccessResult());
            }
        });
    }

    private void example3() {
        System.out.println("Example 3: Earlier task execution with timeout");

        earlier(
                someLongOperation(3000).setTag("Example 3/Operation 1").timeout(2400),
                someLongOperation(2450 - (int) Math.round(Math.random() * 100)).setTag("Example 3/Operation 2"),
                someLongOperation(3200).setTag("Example 3/Operation 3")
        ).onSuccess(new Callback<SuccessEvent<Promise>>() {
            @Override
            public void handle(SuccessEvent<Promise> message) {
                Promise promise = message.getData();
                System.out.println(promise.getTag() + " -> " + promise.getSuccessResult());
            }
        }).onAbort(new Callback<AbortEvent>() {
            @Override
            public void handle(AbortEvent message) {
                Object data = message.getData();
                if (data instanceof Promise) {
                    System.out.println(((Promise) data).getTag() + " -> " + ((Promise) data).getReasonOfTaskCompletion().getData());
                } else if (data instanceof Throwable) {
                    System.out.println(((Throwable) data).getMessage());
                }
            }
        }).timeout(2350 + (int) Math.round(Math.random() * 100));
    }


    private Promise<String> someLongOperation(final int delay) {
        return new AsyncTask<String>() {
            {
                // setEnvironment(?); // can set some environment here

                onAbort(new Callback<AbortEvent>() {
                    @Override
                    public void handle(AbortEvent message) {
                        System.out.println(getTag() + " -> " + getReasonOfTaskCompletion().getData());
                    }
                });
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
                        resolve("resolved");
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
