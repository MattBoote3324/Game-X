package seng302.Common;

/**
 * Created by tho63 on 7/09/17.
 */
public enum GodType {
    ZEUS(0, new GodZeus()), POSEIDON(1, new GodPoseidon()), APHRODITE(2, new GodAphrodite()), ARES(3, new GodAres()), HADES(4, new GodHades());

    final int type;
    final GreekGod god;

    GodType(int type, GreekGod god) {
        this.god = god;
        this.type = type;
    }

    public int getType(){
        return type;
    }

    public GreekGod getGod(){
        return this.god;
    }
}
