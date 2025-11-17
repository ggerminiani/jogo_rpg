package com.gustavo.rpg.entities;

import com.gustavo.rpg.utils.Dice;

public class Mage extends Player {
    public Mage (String name) {
        super(name, 18, 5);
    }

    @Override
    public int attack() {
        int base = getBaseDamage();
        if (getWeapon() != null) base += getWeapon().getDamageBonus();
        return base + Dice.roll(1, 8);
    }
}
