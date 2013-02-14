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

public class Main {

    public static final int WATER = 3;

    public static void main(String[] args) {
        movePlayer(new Player(1));
        movePlayer(new Player(2));
    }

    private static void movePlayer(Player player) {
        int terrain = getTerrainFromPlayerPos(player);

        switch (terrain) {
            case WATER:
                // kom.mix.usage.SwimAbility ability = player.getExtension(kom.mix.usage.FlyAbility.class); // -- Incorrect
                SwimAbility ability = player.getExtension(SwimAbility.class);
                if (ability == null) {
                    System.out.println("You shall not pass!");
                    break;
                }

                ability.apply();
                break;

            default:
                System.out.println("Welcome to Hell!");
        }
    }

    private static int getTerrainFromPlayerPos(Player player) {
        return WATER;
    }
}