package com.gustavo.rpg.core;

public class ConsoleOutput implements GameOutput {

    @Override
    public void println(String s) {
        System.out.println(s);
    }

    @Override
    public void print(String s) {
        System.out.print(s);
    }
}
