package kom.promise;

import kom.promise.events.*;
import kom.util.callback.Callback;

public class UsagePromise {

    public static void main(String[] args) {
        UsagePromise usage = new UsagePromise();
        usage.example1();
        usage.example2();
    }

    private void example1() {
        System.out.println("Example 1: simple example, please wait 3 sec");

        Promise<String> promise = someLongMethod();
        promise.success(new Callback<SuccessEvent<String>>() {
            @Override
            public void handle(SuccessEvent<String> message) {
                System.out.println("Example 1: " + message.getData());
            }
        });
    }

    private void example2() {
        System.out.println("Example 2: timeout example, please wait 2 sec");

        Promise<String> promise = someLongMethod();
        promise.success(new Callback<SuccessEvent<String>>() {
            @Override
            public void handle(SuccessEvent<String> message) {
                System.out.println("Example 2 success: " + message.getData());
            }
        }).halt(new Callback<HaltEvent>() {
            @Override
            public void handle(HaltEvent message) {
                System.out.println("Example 2 halt: " + message.getData().toString());
            }
        }).timeout(2000);
    }



    private Promise<String> someLongMethod() {
        final Deferred<String> deferred = new Deferred<String>();

        new Thread() {
            private boolean isCancelled = false;

            @Override
            public void run() {
                // job stopper
                deferred.getPromise().halt(new Callback<HaltEvent>() {
                    @Override
                    public void handle(HaltEvent message) {
                        isCancelled = true;
                    }
                });

                try {
                    // some long job
                    int i = 0;
                    while (i++ < 300 && !isCancelled) {
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
