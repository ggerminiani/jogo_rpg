package com.gustavo.rpg.items;

public class Weapon extends Item {
    private final int damageBonus;
    public Weapon(String name, int damageBonus) {
        super(name);
        this.damageBonus = damageBonus;
    }
    public int getDamageBonus() { return damageBonus; }
}
