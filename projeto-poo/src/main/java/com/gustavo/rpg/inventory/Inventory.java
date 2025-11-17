package com.gustavo.rpg.inventory;

import java.util.*;

import com.gustavo.rpg.exceptions.InventoryFullException;
import com.gustavo.rpg.exceptions.ItemNotFoundException;
import com.gustavo.rpg.items.Item;

/**
 * [Classe Genérica] Inventário para qualquer subtipo de Item.
 * Mostra uso de List (Collections) e genéricos.
 */
public class Inventory<T extends Item> {
    private final List<T> items = new ArrayList<>();
    private final int capacity;

    public Inventory(int capacity) { this.capacity = capacity; }

    public void add(T item) throws InventoryFullException {
        if (items.size() >= capacity) {
            throw new InventoryFullException();
        }
        items.add(item);
    }

    public void remove(T item) { items.remove(item); }
    public List<T> getItems() { return Collections.unmodifiableList(items); }

    public T findOrThrow(String partial) throws ItemNotFoundException {
        for (T t : items) {
            if (t.getName().toLowerCase().contains(partial.toLowerCase()))
                return t;
        }
        throw new ItemNotFoundException(partial);
    }


    @Override public String toString() { return items.toString(); }
}
