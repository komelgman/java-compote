package kom.mix;

import java.util.Collection;

public interface Extended<T> {
    public boolean  hasExtension(Class<? extends T> name);
    public <E extends T> E getExtension(Class<E> name);
    public Collection<T> getExtensions();
}
