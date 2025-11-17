package com.gustavo.rpg.items;

public class Potion extends Item {
    private final int healAmount;
    public Potion(String name, int healAmount) {
        super(name);
        this.healAmount = healAmount;
    }
    public int getHealAmount() { return healAmount; }
}
