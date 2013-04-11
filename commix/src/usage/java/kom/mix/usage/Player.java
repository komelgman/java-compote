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

package kom.mix.usage;

import kom.mix.Extended;
import kom.mix.Extension;
import kom.mix.ExtensionManager;

import java.util.Collection;

public class Player implements Extended<Extension> {
    private final ExtensionManager<Extension> extman = new ExtensionManager<Extension>();

    public Player(int id) {
        readAbilitiesFromBD(id);
    }

    private void readAbilitiesFromBD(int id) {
        // some logic
        if (id == 1) {
            extman.registerExtension(SwimAbility.class, new SwimAsADog());
        } else {
            //extman.registerExtension(kom.mix.usage.FlyAbility.class, new SwimAsATerminator()); // -- Incorrect
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