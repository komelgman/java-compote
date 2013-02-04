package kom.mix;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: syungman
 * Date: 25.06.12
 * Time: 17:33
 */
public interface Extended<T> {
    public boolean  hasExtension(Class<? extends T> name);
    public <E extends T> E getExtension(Class<E> name);
    public Collection<T> getExtensions();
}
