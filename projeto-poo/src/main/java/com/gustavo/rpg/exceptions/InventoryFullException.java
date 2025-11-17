package com.gustavo.rpg.exceptions;

public class InventoryFullException extends Exception {
    public InventoryFullException() {
        super("Seu inventário está cheio.");
    }
}
