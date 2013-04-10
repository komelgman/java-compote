/*
 * Copyright 2013 Sergey Yungman (aka komelgman)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kom.mix;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExtensionManager<T> implements Extended<T>, Extensible<T> {

    protected ConcurrentMap<Class<?>, T> extensions
            = new ConcurrentHashMap<Class<?>, T>();

    @Override
    @SuppressWarnings("unchecked")
    public <A extends T, B extends A> void registerExtension(B extension) {
        registerExtension((Class<A>) extension.getClass(), extension);
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
    @SuppressWarnings("unchecked")
    public <E extends T> E unregisterExtension(Class<E> name) {
        return (E)extensions.remove(name);
    }

    @Override
    public boolean hasExtension(Class<? extends T> name) {
        return extensions.containsKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends T> E getExtension(Class<E> name) {
        return (E) extensions.get(name);
    }

    @Override
    public Collection<T> getExtensions() {
        return Collections.unmodifiableCollection(extensions.values());
    }
}