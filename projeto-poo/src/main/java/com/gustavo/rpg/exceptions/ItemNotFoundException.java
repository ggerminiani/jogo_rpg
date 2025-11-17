package com.gustavo.rpg.exceptions;

public class ItemNotFoundException extends Exception {
    public ItemNotFoundException(String itemName) {
        super("Item nao encontrado: " + itemName);
    }
}

