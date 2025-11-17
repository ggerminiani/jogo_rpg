package com.gustavo.rpg.entities;

import com.gustavo.rpg.utils.Dice;

public class NPC extends GameCharacter {
    public NPC(String name, int hp, int baseDamage) {
        super(name, hp, baseDamage);
    }

    @Override
    public int attack() {
        return getBaseDamage() + Dice.roll(1, 4);
    }

    @Override
    public String toString() { return getName(); }
}
