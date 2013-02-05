package kom.mix;

import kom.mix.Extension;
import kom.mix.ExtensionManager;
import org.junit.Before;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ExtensionManagerBehavior {

    ExtensionManager manager;

    @Before
    public void before() {
        manager = new ExtensionManager();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenExtensionIsNull() throws Exception {
        final Extension extension = getExtension();
        manager.registerExtension(extension);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenKeyIsNull() throws Exception {
        final Extension extension = new Extension() {};
        manager.registerExtension(null, extension);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenKeyNotNullAndExtensionIsNull() throws Exception {
        manager.registerExtension(Extension.class, null);
    }


    @Test(expected = InvalidParameterException.class)
    public void shouldThrowExceptionWhenExtensionAlreadyRegistered() throws Exception {
        final Extension extension = new Extension() {};

        manager.registerExtension(extension);
        manager.registerExtension(extension);
    }

    @Test
    public void registeredExtensionShouldBeExists() throws Exception {
        final Extension extension = new Extension() {};

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
        final Extension extension = new Extension() {};

        // register extension by his class
        manager.registerExtension(extension);
        manager.unregisterExtension(extension.getClass());
        assertThat(manager.hasExtension(extension.getClass()), is(false));

        // register extension by his superclass
        manager.registerExtension(Extension.class, extension);
        manager.unregisterExtension(Extension.class);
        assertThat(manager.hasExtension(Extension.class), is(false));
    }

    public Extension getExtension() {
        return null;
    }

    private void print(String message) {
        System.out.println(message);
    }
}
