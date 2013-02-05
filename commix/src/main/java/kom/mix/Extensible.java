package kom.mix;

public interface Extensible<T> {
    public <A extends T, B extends A> void registerExtension(B extension);
    public <A extends T, B extends A> void registerExtension(Class<A> name, B extension);
    public void unregisterExtension(Class<? extends T> name);
}
