package com.gustavo.rpg.entities;

import com.gustavo.rpg.utils.Dice;

public class Archer  extends Player {
    public Archer (String name) {
        super(name, 22, 5);
    }

    @Override
    public int attack() {
        int base = getBaseDamage();
        // arqueiro: chance de critico
        int crit = Dice.roll(1, 100);
        if (crit <= 25) { // 25% de chance de critico
            return base * 2;
        }
        return base;
    }
}
