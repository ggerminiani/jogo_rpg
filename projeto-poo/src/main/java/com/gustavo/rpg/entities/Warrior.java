package com.gustavo.rpg.entities;

import com.gustavo.rpg.utils.Dice;

public class Warrior extends Player {
    public Warrior(String name) {
        super(name, 25, 5);
    }

    @Override
    public int attack() {
        int base = getBaseDamage();
        if (getWeapon() != null) base += getWeapon().getDamageBonus();
        return base + Dice.roll(1, 6);
    }
}
