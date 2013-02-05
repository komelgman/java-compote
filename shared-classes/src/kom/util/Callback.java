package kom.util;

/**
 * User: komelgman
 * Date: 9/4/12
 * Time: 12:24 PM
 */
public interface Callback<T> {
    public void handle(T data);
}
