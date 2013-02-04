package kom.mix;

/**
 * Created with IntelliJ IDEA.
 * User: syungman
 * Date: 06.12.12
 * Time: 18:24
 */
public interface Extensible<T> {
    public <A extends T, B extends A> void registerExtension(B extension);
    public <A extends T, B extends A> void registerExtension(Class<A> name, B extension);
    public void unregisterExtension(Class<? extends T> name);
}
