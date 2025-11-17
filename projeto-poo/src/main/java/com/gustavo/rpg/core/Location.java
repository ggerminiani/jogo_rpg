package com.gustavo.rpg.core;

import java.util.*;
import com.gustavo.rpg.entities.NPC;
import com.gustavo.rpg.items.Item;

public class Location {
    private final String name;
    private final String description;

    private final Map<String, Location> exits = new HashMap<>();
    private final List<NPC> npcs = new ArrayList<>();
    private final List<Item> groundItems = new ArrayList<>();

    public Location(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void connect(String direction, Location other) {
        exits.put(direction.toLowerCase(), other);
    }

    public Item findGroundItem(String partial) {
        for (Item item : groundItems) {
            if (item.getName().toLowerCase().contains(partial.toLowerCase())) {
                return item;
            }
        }
        return null;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, Location> getExits() { return exits; }
    public List<NPC> getNpcs() { return npcs; }
    public List<Item> getGroundItems() { return groundItems; }

    @Override public String toString() { return name; }
}
