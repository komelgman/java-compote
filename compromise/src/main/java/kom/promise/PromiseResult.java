package kom.promise;

/**
 * User: syungman
 * Date: 04.02.13
 */
public class PromiseResult<T> {
    final T data;

    public PromiseResult(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
