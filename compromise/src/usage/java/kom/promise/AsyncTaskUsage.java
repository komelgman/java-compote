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
