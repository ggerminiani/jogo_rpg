package com.gustavo.rpg;

import java.util.Scanner;

import com.gustavo.rpg.core.Game;

public class Main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        boolean rodando = true;

        while (rodando) {
            System.out.println("====================================");
            System.out.println("         RPG ***JOGER***");
            System.out.println("====================================");
            System.out.println("1 - Novo jogo");
            System.out.println("2 - Carregar jogo");
            System.out.println("3 - Sair");
            System.out.print("Escolha uma opcao: ");

            String opcao = in.nextLine().trim();

            switch (opcao.toLowerCase()) {
                case "1", "novo", "novo jogo" -> novoJogo(in);
                case "2", "carregar", "carregar jogo" -> carregarJogo(in);
                case "3", "sair", "exit", "q" -> {
                    System.out.println("Saindo. Ate logo!");
                    rodando = false;
                }
                
                default -> {
                    System.out.println("Opcao invalida. Tente novamente.");
                }
            }
        }

        in.close();
    }

    private static void novoJogo(Scanner in) {
        System.out.print("Digite o nome do jogador: ");
        String nome = in.nextLine();

        System.out.println("Escolha a classe:");
        System.out.println("1 - Guerreiro");
        System.out.println("2 - Mago");
        System.out.println("3 - Arqueiro");
        System.out.print("Opcao: ");
        String opClasse = in.nextLine();

        String classe = switch (opClasse) {
            case "2" -> "mago";
            case "3" -> "arqueiro";
            default  -> "guerreiro";
        };

        Game jogo = new Game(nome, classe);
        jogo.run();
    }

    private static void carregarJogo(Scanner in) {
        try {
            java.nio.file.Path dir = java.nio.file.Paths.get("saves");
            if (!java.nio.file.Files.exists(dir)) {
                System.out.println("Nao ha jogos salvos ainda.");
                return;
            }

            var arquivos = java.nio.file.Files.list(dir)
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .sorted()
                    .toList();

            if (arquivos.isEmpty()) {
                System.out.println("Nao ha jogos salvos ainda.");
                return;
            }

            System.out.println("Jogos salvos:");
            for (int i = 0; i < arquivos.size(); i++) {
                System.out.println((i + 1) + " - " + arquivos.get(i).getFileName());
            }

            System.out.print("Escolha um numero (ou ENTER para cancelar): ");
            String linha = in.nextLine().trim();
            if (linha.isEmpty()) {
                System.out.println("Carregamento cancelado.");
                return;
            }

            int escolha;
            try {
                escolha = Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida.");
                return;
            }

            if (escolha < 1 || escolha > arquivos.size()) {
                System.out.println("Numero fora da faixa.");
                return;
            }

            java.nio.file.Path arquivoEscolhido = arquivos.get(escolha - 1);
            System.out.println("Carregando jogo de: " + arquivoEscolhido.getFileName());

            Game game = Game.carregarDeArquivo(arquivoEscolhido);
            game.run();

        } catch (java.io.IOException e) {
            System.out.println("Erro ao listar jogos salvos: " + e.getMessage());
        }
    }

}



