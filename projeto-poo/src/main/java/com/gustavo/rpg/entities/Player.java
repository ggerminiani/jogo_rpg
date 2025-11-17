package com.gustavo.rpg.entities;

import com.gustavo.rpg.inventory.Inventory;
import com.gustavo.rpg.items.Item;
import com.gustavo.rpg.items.Weapon;

public abstract class Player extends GameCharacter {
    private final Inventory<Item> bag = new Inventory<>(20); // [Genéricos + Composição]
    private Weapon weapon; // [Agregação]

    protected Player(String name, int hp, int baseDamage) {
        super(name, hp, baseDamage);
    }

    public Inventory<Item> getBag() { return bag; }
    public void setWeapon(Weapon weapon) { this.weapon = weapon; }
    public Weapon getWeapon() { return weapon; }

    public String describe() {
        return "Jogador: " + getName()
            + " | HP=" + getHp()
            + (weapon != null ? " | Arma: " + weapon.getName() : " | Sem arma");
    }

}
