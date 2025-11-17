package com.gustavo.rpg.utils;

import java.util.concurrent.ThreadLocalRandom;

public final class Dice {
    private Dice() {}
    public static int roll(int qtd, int faces) {
        int sum = 0;
        for (int i = 0; i < qtd; i++) {
            sum += ThreadLocalRandom.current().nextInt(1, faces + 1);
        }
        return sum;
    }
}
