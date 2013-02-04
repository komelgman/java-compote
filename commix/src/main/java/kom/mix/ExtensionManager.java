package kom.mix;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created with IntelliJ IDEA.
 * User: syungman
 * Date: 25.06.12
 * Time: 17:37
 */
public class ExtensionManager<T> implements Extended<T>, Extensible<T> {

    protected ConcurrentMap<Class<?>, T> extensions
            = new ConcurrentHashMap<Class<?>, T>();

    @Override
    @SuppressWarnings("unchecked")
    public <A extends T, B extends A> void registerExtension(B extension) {
        registerExtension((Class<A>)extension.getClass(), extension);
    }

    @Override
    public <A extends T, B extends A> void registerExtension(Class<A> name, B extension) {
        if (extension == null) {
            throw new NullPointerException("Can't register NULL as extension");
        }

        if (name == null) {
            throw new NullPointerException("Can't register extension with NULL key");
        }

        if (extensions.putIfAbsent(name, extension) != null) {
            throw new InvalidParameterException("Extension with key:" + name.getCanonicalName() + " already registered");
        }
    }

    @Override
    public void unregisterExtension(Class<? extends T> name) {
        extensions.remove(name);
    }

    @Override
    public boolean hasExtension(Class<? extends T> name) {
        return extensions.containsKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends T> E getExtension(Class<E> name) {
        return (E)extensions.get(name);
    }

    @Override
    public Collection<T> getExtensions() {
        return extensions.values();
    }
}