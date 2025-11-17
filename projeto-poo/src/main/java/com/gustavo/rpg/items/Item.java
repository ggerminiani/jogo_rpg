package com.gustavo.rpg.items;

/** [Classe Abstrata] Itens base. */
public abstract class Item {
    private final String name; // [Encapsulamento]
    protected Item(String name) { this.name = name; }
    public String getName() { return name; }
    @Override public String toString() { return name; }
}
