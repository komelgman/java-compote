package kom.promise;

/**
 * User: syungman
 * Date: 04.02.13
 */
final class PromiseFactory {

    // todo: create object pool

    public static <T> Promise<T> getPromise() {
        return new Promise<T>();
    }
}
