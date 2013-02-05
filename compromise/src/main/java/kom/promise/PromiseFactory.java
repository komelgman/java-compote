package kom.promise;

final class PromiseFactory {

    // todo: create object pool

    public static <T> Promise<T> getPromise() {
        return new Promise<T>();
    }
}
