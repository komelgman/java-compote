Compromise
==========

Compromise is a Deferred/Promise pattern Java implementation

Dependency
----------
This module depends on Compliment module and shared-classes

Features
--------
* ```Deferred```, ```Promise``` and ```AsyncTask``` objects;
* ```Promise<T> extends Future<T>```;
* Deferred methods: ```.resolve("ok"), .reject("oops"), .update("please wait: 10% completed")```;
* Promise termination: ```.cancel("Avada Kedavra"), .timeout(msecs)```;
* Promise callbacks: ```.onSuccess(...), .onFail(...), .onUpdate(...), .onCancel(...), .onAny(...)```;
* Synchronization: ```.await()```;
* AsyncUtils:
  - ```.wrap(Callable...);``` create promise from callable
  - ```.chain(asyncTask1, asyncTask2, ...);``` asyncTasks sequential execution
  - ```.parallel(p1, p2, ...);``` wait for all promises will successfully fulfilled (reject on first failed/aborted)
  - ```.earlier(p1, p2, ...);``` wait for first promise will fulfilled (resolve on success, reject on fail/abort)
* AsyncContext - provides access to some async stuffs (threadExecutor, callbackExecutor, scheduler, ...);

Examples (more can be found in src/usage directory)
--------
###Async method example
```Java
Promise<ResultType> someAsyncMethod() {
    final Deferred<ResultType> deferred = ...
    final SomeAsyncObject asyncObject = ...;

    deferred.onCancel(new Callback<CancelEvent>() {
        @Override
        public void handle(CancelEvent message) {
            // stopping async code
            asyncObject.cancel();
        }
    });

    defaultContext().executeRunnable(new Runnable() {
        @Override
        public void run() {
            try {
                // retrieving async code result
                deferred.resolve(asyncObject.getResult());
            } catch (Exception e) {
                // some error caused
                deferred.reject(e);
            }
        }
    });

    return deferred.getPromise();
}
```
###Deferred and Promise usage
Creations:
```Java
// from default async context
Deferred<ResultType> deferred = AsyncUtils.defaultContext().deferred();
...
return deferred.getPromise();
```
or
```Java
// with custom context
AsyncContext context = new MyAsyncContext();
Deferred<ResultType> deferred1 = context.deferred();
Deferred<ResultType> deferred2 = new DeferredImpl(context);
```
Callbacks:
```Java
Promise<ResultType> promise = ...
promise.onSuccess(new Callback<SuccessEvent<ResultType>>() {
    @Override
    public void handle(SuccessEvent<ResultType> message) {
        ResultType result = message.getData();
        System.out.println("task result: " + result.toString());
    }
}).onFail(new Callback<FailEvent>() {
    @Override
    public void handle(FailEvent message) {
        Object reason = message.getData();
        System.out.println("retrieving reason: " + reason.toString());
    }
}).onAny(new Callback<PromiseEvent>() {
    @Override
    public void handle(PromiseEvent message) {
        Class<?> event = message.getClass();
        Object data = message.getData();
        if (event == SuccessEvent.class) {
            ...
        }
        ...
    }
});
```