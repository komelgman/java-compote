package kom.promise;

public class PromiseResult<T> {
    final T data;

    public PromiseResult(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
