import kom.mix.Extended;
import kom.mix.Extension;
import kom.mix.ExtensionManager;

import java.util.Collection;

public class Player implements Extended<Extension> {
    private final ExtensionManager<Extension> extman = new ExtensionManager();

    public Player(int id) {
        readAbilitiesFromBD(id);
    }

    private void readAbilitiesFromBD(int id) {
        // some logic
        if (id == 1) {
            extman.registerExtension(SwimAbility.class, new SwimAsADog());
        } else {
            //extman.registerExtension(FlyAbility.class, new SwimAsATerminator()); // -- Incorrect
            extman.registerExtension(SwimAbility.class, new SwimAsATerminator());
        }
    }

    static class SwimAsADog implements SwimAbility {
        @Override
        public void apply() {
            System.out.println("You safely swim!");
        }
    }

    static class SwimAsATerminator implements SwimAbility {
        @Override
        public void apply() {
            System.out.println("I'll be back!");
        }
    }

    @Override
    public boolean hasExtension(Class<? extends Extension> name) {
        return extman.hasExtension(name);
    }

    @Override
    public <E extends Extension> E getExtension(Class<E> name) {
        return extman.getExtension(name);
    }

    @Override
    public Collection<Extension> getExtensions() {
        return extman.getExtensions();
    }
}