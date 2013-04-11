/**
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
import static org.junit.Assert.*;

public class ExtensionManagerBehavior {
    private ExtensionManager<RootExtension> manager;
    private NestedExtension nestedExtension;
    private final RootExtension nullExtension = null;

    @Before
    public void before() {
        manager = new ExtensionManager<RootExtension>();
        nestedExtension = new NestedExtension();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenExtensionIsNull() throws Exception {
        manager.registerExtension(nullExtension);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenKeyIsNull() throws Exception {
        manager.registerExtension(null, nestedExtension);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenKeyNotNullAndExtensionIsNull() throws Exception {
        manager.registerExtension(RootExtension.class, null);
    }


    @Test(expected = InvalidParameterException.class)
    public void shouldThrowExceptionWhenExtensionAlreadyRegistered() throws Exception {
        manager.registerExtension(nestedExtension);
        manager.registerExtension(nestedExtension);
    }

    @Test
    public void registeredExtensionShouldBeExists() throws Exception {
        // register extension by his class
        assertThat(manager.hasExtension(NestedExtension.class), is(false));
        manager.registerExtension(nestedExtension);
        assertThat(manager.hasExtension(NestedExtension.class), is(true));

        // or register extension by his superclass
        assertThat(manager.hasExtension(RootExtension.class), is(false));
        manager.registerExtension(RootExtension.class, nestedExtension);
        assertThat(manager.hasExtension(RootExtension.class), is(true));
    }

    @Test
    public void registeredExtensionShouldBeReturns() throws Exception {
        // register extension by his class
        assertNull(manager.getExtension(NestedExtension.class));
        manager.registerExtension(nestedExtension);
        assertEquals(nestedExtension, manager.getExtension(NestedExtension.class));

        // or register extension by his superclass
        assertNull(manager.getExtension(RootExtension.class));
        manager.registerExtension(RootExtension.class, nestedExtension);
        assertEquals(nestedExtension, manager.getExtension(RootExtension.class));
    }

    @Test
    public void unregisteredExtensionShouldBeNotExists() throws Exception {
        // register extension by his class
        manager.registerExtension(nestedExtension);
        manager.unregisterExtension(NestedExtension.class);
        assertThat(manager.hasExtension(NestedExtension.class), is(false));

        // or register extension by his superclass
        manager.registerExtension(RootExtension.class, nestedExtension);
        manager.unregisterExtension(RootExtension.class);
        assertThat(manager.hasExtension(RootExtension.class), is(false));
    }

    @Test
    public void unregisteredExtensionShouldBeNotReturns() throws Exception {
        // register extension by his class
        manager.registerExtension(nestedExtension);
        manager.unregisterExtension(NestedExtension.class);
        assertNull(manager.getExtension(NestedExtension.class));

        // or register extension by his superclass
        manager.registerExtension(RootExtension.class, nestedExtension);
        manager.unregisterExtension(RootExtension.class);
        assertNull(manager.getExtension(RootExtension.class));
    }

    @Test
    public void allExtensionShouldBeReturns() throws Exception {
        // register extension by his class
        manager.registerExtension(nestedExtension);

        // or register extension by his superclass
        manager.registerExtension(RootExtension.class, nestedExtension);

        assertNotNull(manager.getExtensions());
        assertEquals(2, manager.getExtensions().size());
    }


    private class NestedExtension extends RootExtension {
    }

    private class RootExtension implements Extension {
    }
}
