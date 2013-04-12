Compromise
==========

Compromise is a Java Deferred/Promise pattern realisation

Dependency
----------
This module depends on Compliment module and shared-classes

Features
--------
* Deferred, Promise and AsyncTask objects;
* Deferred: ```.resolve("ok"), .reject("oops"), .update("please wait: 10% completed")```;
* Promise termination: ```.abort("Avada Kedavra"), .timeout(msecs)```;
* Promise callbacks: ```.onSuccess(...), .onFail(...), .onUpdate(...), .onAbort(...), .onAny(...)```;
* Synchronization: ```.await()```;
* AsyncUtils:
  - ```.wrap(Future...);``` create promise from java future
  - ```.wrap(Callable...);``` create promise from callable
  - ```.chain(asyncTask1, asyncTask2, ...);``` asyncTasks sequential execution
  - ```.parallel(p1, p2, ...);``` wait for all promises will successfully fulfilled (reject on first failed/aborted)
  - ```.earlier(p1, p2, ...);``` wait for first promise will fulfilled (resolve on success, reject on fail/abort)
* AsyncContext - provides access to some async stuffs (executor, callbackExecutor, scheduler ...);

Examples
--------
### Simple Deferred and Promise usage
Create deferred object:
```Java
// from default async context
Deferred<ResultType> deferred = AsyncContext.defaultContext().deferred();
```
or
```Java
// with custom context
AsyncContext context = new MyAsyncContext();
Deferred<ResultType> deferred1 = context.deferred();
Deferred<ResultType> deferred2 = new DeferredImpl(context);
```
