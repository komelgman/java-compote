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