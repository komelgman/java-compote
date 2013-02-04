package kom.util;

/**
 * User: komelgman
 * Date: 9/20/12
 * Time: 10:54 AM
 */
public abstract class RunnableCallback<T> implements Runnable, Callback<T>  {

    private T data;

    @Override
    public final void handle(T data) {
        this.data = data;
    }

    public final T getData() {
        return data;
    }
}
