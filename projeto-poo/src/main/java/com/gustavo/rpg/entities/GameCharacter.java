package com.gustavo.rpg.entities;

import com.gustavo.rpg.core.Location;

/**
 * [Classe Abstrata] Base para personagens.
 * Demonstra encapsulamento e polimorfismo (método attack()).
 */
public abstract class GameCharacter {
    private String name;       // [Encapsulamento]
    private int hp;            // [Encapsulamento]
    private int baseDamage;    // [Encapsulamento]
    private Location location; // personagem "está em" uma Location

    protected GameCharacter(String name, int hp, int baseDamage) {
        this.name = name;
        this.hp = hp;
        this.baseDamage = baseDamage;
    }

    public String getName() { return name; }
    public int getHp() { return hp; }
    public boolean isAlive() { return hp > 0; }
    public void setLocation(Location location) { this.location = location; }
    public Location getLocation() { return location; }

    public void takeDamage(int amount) { this.hp = Math.max(0, this.hp - amount); }
    public void heal(int amount) { this.hp += amount; }
    public int getBaseDamage() { return baseDamage; }

    // [Polimorfismo] cada subclasse define o cálculo
    public abstract int attack();
}
