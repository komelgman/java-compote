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

import org.junit.Before;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ExtensionManagerBehavior {

    ExtensionManager<Extension> manager;

    @Before
    public void before() {
        manager = new ExtensionManager<Extension>();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenExtensionIsNull() throws Exception {
        final Extension extension = getNullExtension();
        manager.registerExtension(extension);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenKeyIsNull() throws Exception {
        final Extension extension = new Extension() {
        };
        manager.registerExtension(null, extension);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenKeyNotNullAndExtensionIsNull() throws Exception {
        manager.registerExtension(Extension.class, null);
    }


    @Test(expected = InvalidParameterException.class)
    public void shouldThrowExceptionWhenExtensionAlreadyRegistered() throws Exception {
        final Extension extension = new Extension() {
        };

        manager.registerExtension(extension);
        manager.registerExtension(extension);
    }

    @Test
    public void registeredExtensionShouldBeExists() throws Exception {
        final Extension extension = new Extension() {
        };

        // register extension by his class
        assertThat(manager.hasExtension(extension.getClass()), is(false));
        manager.registerExtension(extension);
        assertThat(manager.hasExtension(extension.getClass()), is(true));

        // register extension by his superclass
        assertThat(manager.hasExtension(Extension.class), is(false));
        manager.registerExtension(Extension.class, extension);
        assertThat(manager.hasExtension(Extension.class), is(true));
    }

    @Test
    public void unregisteredExtensionShouldBeNotExists() throws Exception {
        final Extension extension = new Extension() {
        };

        // register extension by his class
        manager.registerExtension(extension);
        manager.unregisterExtension(extension.getClass());
        assertThat(manager.hasExtension(extension.getClass()), is(false));

        // register extension by his superclass
        manager.registerExtension(Extension.class, extension);
        manager.unregisterExtension(Extension.class);
        assertThat(manager.hasExtension(Extension.class), is(false));
    }

    public Extension getNullExtension() {
        return null;
    }
}
