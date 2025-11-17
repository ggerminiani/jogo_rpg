package com.gustavo.rpg.core;

import com.gustavo.rpg.entities.*;
import com.gustavo.rpg.items.*;
import com.gustavo.rpg.utils.Dice;
import com.gustavo.rpg.exceptions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import java.util.*;
import java.util.stream.Collectors;

public class Game {

    /**
     * Estrutura interna para representar dados de save.
     * (Por enquanto nao guardamos a classe do jogador aqui.)
     */
    private static class SaveData {
        String name;
        String playerClass;
        int hp;
        String localKey;
        String weaponName;
        java.util.List<String> inventoryNames;
    }

    private final Scanner in = new Scanner(System.in);
    private final World world = new World();
    private Player player;

    private final String playerName;
    private final String playerClass; // classe escolhida (guerreiro/mago/arqueiro)
    private final SaveData loaded;    // se nao for null, significa jogo carregado
    private final GameOutput out;     // saida (console ou GUI)

    // -------------------------------------------------------------------------
    // CONSTRUTORES
    // -------------------------------------------------------------------------

    // construtor para novo jogo (console) - classe padrão: guerreiro
    public Game(String playerName) {
        this(playerName, "guerreiro", new ConsoleOutput());
    }

    // construtor para novo jogo (GUI) - classe padrão: guerreiro
    public Game(String playerName, GameOutput out) {
        this(playerName, "guerreiro", out);
    }

    // construtor para novo jogo especificando a classe (console)
    public Game(String playerName, String playerClass) {
        this(playerName, playerClass, new ConsoleOutput());
    }

    // construtor para novo jogo especificando classe + saída customizada (GUI)
    public Game(String playerName, String playerClass, GameOutput out) {
        if (playerName == null || playerName.isBlank()) {
            this.playerName = "Artemis";
        } else {
            this.playerName = playerName.trim();
        }
        this.playerClass = (playerClass == null || playerClass.isBlank())
                ? "guerreiro"
                : playerClass.toLowerCase();
        this.loaded = null;
        this.out = out;
    }

    // construtor para jogo carregado (console)
    private Game(SaveData data) {
        this(data, new ConsoleOutput());
    }

    // construtor para jogo carregado com saída customizada (GUI)
    private Game(SaveData data, GameOutput out) {
        this.playerName = data.name;
        this.playerClass = (data.playerClass == null || data.playerClass.isBlank())
            ? "guerreiro"
            : data.playerClass.toLowerCase();
        this.loaded = data;
        this.out = out;
    }

    // -------------------------------------------------------------------------
    // CICLO DO JOGO (CONSOLE)
    // -------------------------------------------------------------------------

    /**
     * Usado por console e GUI: monta o mundo e imprime mensagem inicial.
     */
    public void start() {
        boot();
        out.println("Bem-vindo! Digite 'ajuda' para comandos, 'sair' para encerrar.");
    }

    /**
     * Loop de jogo no modo console (Main usa este).
     */
    public void run() {
        start();  // inicializa o mundo + mensagens iniciais

        boolean vivo = true;
        while (vivo) {
            out.print("> ");
            String linha = in.nextLine().trim();
            vivo = handleCommand(linha);
        }
        out.println("Ate mais!");
    }

    /**
     * Trata um comando (tanto no console quanto na GUI).
     * Retorna false se o jogador pediu para sair.
     */
    public boolean handleCommand(String linha) {
        linha = linha.trim();
        String[] partes = linha.split("\\s+", 2);
        String comando = partes[0].toLowerCase();
        String arg = partes.length > 1 ? partes[1] : "";

        try {
            switch (comando) {
                case "ajuda" -> ajuda();
                case "status" -> mostrarStatus();
                case "olhar" -> olhar();
                case "inventario" -> mostrarInventario();
                case "ir" -> ir(arg);
                case "atacar" -> atacar();
                case "pegar" -> pegar(arg);
                case "largar" -> largar(arg);
                case "equipar" -> equipar(arg);
                case "beber" -> beber(arg);
                case "salvar" -> salvar();
                case "sair" -> {
                    return false; // encerra o jogo
                }
                case "" -> { /* ignora vazio */ }
                default -> out.println("Comando desconhecido. Tente 'ajuda'.");
            }
        } catch (InvalidActionException |
                 ItemNotFoundException |
                 InventoryFullException e) {
            out.println("Erro: " + e.getMessage());
        }

        return true; // continua jogando
    }

    // -------------------------------------------------------------------------
    // COMANDOS
    // -------------------------------------------------------------------------

    private void ajuda() {
        println("Comandos:");
        println("  ajuda               - lista comandos");
        println("  olhar               - descreve o local atual");
        println("  ir <direcao>        - move (norte/sul/leste/oeste)");
        println("  status              - mostra status do jogador");
        println("  inventario          - lista itens na mochila");
        println("  pegar <item>        - pega item do chao");
        println("  largar <item>       - larga item no chao");
        println("  equipar <arma>      - equipa uma arma do inventario");
        println("  beber <pocao>       - bebe uma pocao do inventario");
        println("  atacar              - ataca inimigo no local");
        println("  salvar              - salva o jogo em arquivo");
        println("  sair                - encerra o jogo");
    }

    private void boot() {
        // =========================
        // 1) CRIA LOCAIS
        // =========================
        Location vila = new Location("Vila", "Uma vila tranquila, com poucas casas e uma pequena taverna.");
        Location bosque = new Location("Bosque", "Sons estranhos entre as arvores. A luz do sol mal chega ao chao.");
        Location ruinas = new Location("Ruinas", "Restos de uma antiga fortaleza, tomada pelo tempo.");
        Location torre = new Location("Torre", "Uma torre de mago em ruinas, ainda pulsando com energia arcana.");

        // Conexoes entre locais
        vila.connect("norte", bosque);

        bosque.connect("sul", vila);
        bosque.connect("leste", ruinas);

        ruinas.connect("oeste", bosque);
        ruinas.connect("norte", torre);

        torre.connect("sul", ruinas);

        // =========================
        // 2) INIMIGOS
        // =========================
        NPC lobo = new NPC("Lobo", 12, 3);
        NPC esqueleto = new NPC("Esqueleto", 18, 4);
        NPC magoSombrio = new NPC("Mago Sombrio", 22, 5);

        bosque.getNpcs().add(lobo);
        ruinas.getNpcs().add(esqueleto);
        torre.getNpcs().add(magoSombrio);

        // =========================
        // 3) ITENS NO CHAO
        // =========================
        vila.getGroundItems().add(new Weapon("Espada curta", 4));
        bosque.getGroundItems().add(new Potion("Pocao de cura", 6));
        ruinas.getGroundItems().add(new Weapon("Espada longa", 6));
        torre.getGroundItems().add(new Weapon("Cajado arcano", 7));

        // =========================
        // 4) REGISTRA LOCAIS NO MUNDO
        // =========================
        world.getLocations().put("vila", vila);
        world.getLocations().put("bosque", bosque);
        world.getLocations().put("ruinas", ruinas);
        world.getLocations().put("torre", torre);

        // =========================
        // 5) DEFINE O PLAYER
        // =========================
        if (loaded == null) {
            // NOVO JOGO usando a classe escolhida
            player = criarPlayerParaClasse(playerName, playerClass);
            player.setLocation(vila);

            // arma inicial basica
            player.setWeapon(new Weapon("Espada de treino", 3));

            println("Voce esta na Vila. Tente 'olhar' ou 'ir norte'.");
        } else {
            // JOGO CARREGADO: respeita a classe salva
            player = criarPlayerParaClasse(loaded.name, playerClass);

            // HP base depende da classe (Warrior, Mage, Archer)
            int baseHp = player.getHp(); // HP inicial da classe
            if (loaded.hp < baseHp) {
                player.takeDamage(baseHp - loaded.hp);
            } else if (loaded.hp > baseHp) {
                player.heal(loaded.hp - baseHp);
            }

            // Local
            Location destino = world.getLocations().getOrDefault(loaded.localKey, vila);
            player.setLocation(destino);

            // Arma equipada
            if (loaded.weaponName != null && !loaded.weaponName.equals("-")) {
                Weapon w = criarWeaponAPartirDoNome(loaded.weaponName);
                player.setWeapon(w);
            }

            // Inventario
            for (String nomeItem : loaded.inventoryNames) {
                if (nomeItem == null || nomeItem.isBlank()) continue;
                Item item = criarItemAPartirDoNome(nomeItem);
                if (item != null) {
                    try {
                        player.getBag().add(item);
                    } catch (InventoryFullException e) {
                        println("Aviso: inventario cheio ao carregar item: " + nomeItem);
                    }
                }
            }

            println("Jogo carregado para o jogador: " + player.getName());
            println("Use 'status' ou 'olhar' para ver o estado atual.");
        }

    }

    private void mostrarStatus() {
        String classe = player.getClass().getSimpleName(); // Warrior, Mage, Archer...

        // se quiser deixar em PT-BR bonitinho:
        String classePt = switch (classe) {
            case "Mage" -> "Mago";
            case "Archer" -> "Arqueiro";
            case "Warrior" -> "Guerreiro";
            default -> classe;
        };

        String arma = (player.getWeapon() != null)
                ? player.getWeapon().getName()
                : "nenhuma";

        out.println("Jogador: " + player.getName()
                + " (" + classePt + ")"
                + " | HP=" + player.getHp()
                + " | Arma: " + arma);
    }

    private void olhar() {
        Location loc = player.getLocation();
        println("Local: " + loc.getName());
        println("Descricao: " + loc.getDescription());

        if (!loc.getGroundItems().isEmpty()) {
            println("Itens no chao: " + loc.getGroundItems());
        } else {
            println("Nao ha itens no chao.");
        }

        if (!loc.getNpcs().isEmpty()) {
            println("Voce ve: " + loc.getNpcs());
        } else {
            println("Nao ha inimigos aqui.");
        }

        println("Saidas disponiveis: " + loc.getExits().keySet());
    }

    private void ir(String direcao) {
        if (direcao == null || direcao.isBlank()) {
            println("Use: ir <direcao> (norte, sul, leste, oeste)");
            return;
        }

        Location atual = player.getLocation();
        Location destino = atual.getExits().get(direcao.toLowerCase());

        if (destino == null) {
            println("Nao existe caminho para " + direcao + " a partir daqui.");
            return;
        }

        player.setLocation(destino);
        println("Voce se moveu para: " + destino.getName());
        olhar(); // mostra o novo local automaticamente
    }

    private void atacar() {
        Location loc = player.getLocation();
        if (loc.getNpcs().isEmpty()) {
            println("Nao ha inimigos aqui.");
            return;
        }

        GameCharacter alvo = loc.getNpcs().get(0);

        int dano = player.attack();
        alvo.takeDamage(dano);
        println(player.getName() + " causou " + dano + " em " + alvo.getName() + ".");

        if (!alvo.isAlive()) {
            println("Voce derrotou " + alvo.getName() + "!");
            loc.getNpcs().remove(0);
            return;
        }

        int contra = Dice.roll(1, 4);
        player.takeDamage(contra);
        println(alvo.getName() + " contra-atacou por " + contra + ".");
    }

    private void pegar(String nomeItem)
            throws InvalidActionException, InventoryFullException {
        if (nomeItem == null || nomeItem.isBlank()) {
            throw new InvalidActionException("Use: pegar <nome do item>");
        }

        Location loc = player.getLocation();
        Item item = loc.findGroundItem(nomeItem);
        if (item == null) {
            throw new InvalidActionException("Nao existe esse item no chao aqui.");
        }

        player.getBag().add(item); // pode disparar InventoryFullException
        loc.getGroundItems().remove(item);
        println("Voce pegou: " + item.getName());
    }

    private void largar(String nomeItem)
            throws InvalidActionException, ItemNotFoundException {
        if (nomeItem == null || nomeItem.isBlank()) {
            throw new InvalidActionException("Use: largar <nome do item>");
        }

        Item item = player.getBag().findOrThrow(nomeItem);
        player.getBag().remove(item);
        player.getLocation().getGroundItems().add(item);
        println("Voce largou: " + item.getName());
    }

    private void equipar(String nomeItem)
            throws InvalidActionException, ItemNotFoundException {
        if (nomeItem == null || nomeItem.isBlank()) {
            throw new InvalidActionException("Use: equipar <nome da arma>");
        }

        Item item = player.getBag().findOrThrow(nomeItem);
        if (!(item instanceof Weapon)) {
            throw new InvalidActionException("Isso nao e uma arma.");
        }

        Weapon w = (Weapon) item;
        player.setWeapon(w);
        println("Voce equipou: " + w.getName());
    }

    private void beber(String nomeItem)
            throws InvalidActionException, ItemNotFoundException {
        if (nomeItem == null || nomeItem.isBlank()) {
            throw new InvalidActionException("Use: beber <nome da pocao>");
        }

        Item item = player.getBag().findOrThrow(nomeItem);
        if (!(item instanceof Potion)) {
            throw new InvalidActionException("Isso nao e uma pocao.");
        }

        Potion p = (Potion) item;
        player.heal(p.getHealAmount());
        player.getBag().remove(p);
        println("Voce bebeu " + p.getName()
                + " e recuperou " + p.getHealAmount() + " de HP.");
    }

    private void mostrarInventario() {
        var itens = player.getBag().getItems();
        if (itens.isEmpty()) {
            println("Seu inventario esta vazio.");
            return;
        }
        println("Itens no inventario:");
        for (int i = 0; i < itens.size(); i++) {
            println("  " + (i + 1) + " - " + itens.get(i).getName());
        }
    }

    private void salvar() {
        try {
            Path dir = Paths.get("saves");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // gerar nome amigavel: Player__2025-11-16__21-53-01.txt
            String nomePlayer = normalizarNome(player.getName());

            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd__HH-mm-ss"));

            String nomeArquivo = nomePlayer + "__" + timestamp + ".txt";

            Path arquivo = dir.resolve(nomeArquivo);

            // descobre a "chave" da location atual (ex: "vila" / "bosque")
            String localKey = encontrarChaveDaLocation(player.getLocation());

            String weaponName = (player.getWeapon() != null)
                    ? player.getWeapon().getName()
                    : "-";

            String inventario = player.getBag().getItems().stream()
                    .map(Item::getName)
                    .collect(Collectors.joining(","));

            String classeSalvar;
            if (player instanceof Mage) {
                classeSalvar = "mago";
            } else if (player instanceof Archer) {
                classeSalvar = "arqueiro";
            } else {
                classeSalvar = "guerreiro";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(player.getName()).append("\n");
            sb.append(classeSalvar).append("\n");
            sb.append(player.getHp()).append("\n");
            sb.append(localKey).append("\n");
            sb.append(weaponName).append("\n");
            sb.append(inventario).append("\n");

            Files.writeString(arquivo, sb.toString(), StandardCharsets.UTF_8);

            println("Jogo salvo em: " + arquivo.toAbsolutePath());
        } catch (IOException e) {
            println("Erro ao salvar o jogo: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Player criarPlayerParaClasse(String nome, String classe) {
        if (classe == null) classe = "guerreiro";
        classe = classe.toLowerCase();

        return switch (classe) {
            case "mago"     -> new Mage(nome);
            case "arqueiro" -> new Archer(nome);
            default         -> new Warrior(nome);
        };
    }

    private String encontrarChaveDaLocation(Location loc) {
        for (Map.Entry<String, Location> entry : world.getLocations().entrySet()) {
            if (entry.getValue() == loc) {
                return entry.getKey();
            }
        }
        return "vila";
    }

    private String normalizarNome(String nome) {
        String normalizado = nome
                .replaceAll("[^a-zA-Z0-9 ]", "")
                .replaceAll(" ", "_");

        if (normalizado.isEmpty()) {
            normalizado = "player";
        }
        return normalizado;
    }

    private Weapon criarWeaponAPartirDoNome(String nome) {
        if (nome.toLowerCase().contains("espada curta")) {
            return new Weapon(nome, 4);
        }
        return new Weapon(nome, 3);
    }

    private Item criarItemAPartirDoNome(String nome) {
        String lower = nome.toLowerCase();
        if (lower.contains("espada")) {
            return criarWeaponAPartirDoNome(nome);
        } else if (lower.contains("pocao")) {
            return new Potion(nome, 6);
        }
        return null;
    }

    private void println(String s) { out.println(s); }
    private void print(String s)   { out.print(s); }

    // -------------------------------------------------------------------------
    // LOAD DE JOGO
    // -------------------------------------------------------------------------

    public static Game carregarDeArquivo(Path arquivo) {
        return carregarDeArquivo(arquivo, new ConsoleOutput());
    }

    public static Game carregarDeArquivo(Path arquivo, GameOutput out) {
        try {
            List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);

            if (linhas.size() < 5) {
                System.out.println("Arquivo de save invalido: " + arquivo);
                return new Game("Player", out);
            }

            SaveData data = new SaveData();

            // Suporte a DOIS formatos:
            // novo (6 linhas): nome, classe, hp, local, arma, inventario
            // antigo (5 linhas): nome, hp, local, arma, inventario  -> classe = guerreiro
            String invLine;

            if (linhas.size() >= 6) {
                // NOVO FORMATO
                data.name = linhas.get(0).trim();
                data.playerClass = linhas.get(1).trim().toLowerCase();
                data.hp = Integer.parseInt(linhas.get(2).trim());
                data.localKey = linhas.get(3).trim();
                data.weaponName = linhas.get(4).trim();
                invLine = linhas.get(5).trim();
            } else {
                // FORMATO ANTIGO (sem classe)
                data.name = linhas.get(0).trim();
                data.playerClass = "guerreiro"; // default para saves antigos
                data.hp = Integer.parseInt(linhas.get(1).trim());
                data.localKey = linhas.get(2).trim();
                data.weaponName = linhas.get(3).trim();
                invLine = linhas.get(4).trim();
            }

            if (invLine.isEmpty()) {
                data.inventoryNames = java.util.List.of();
            } else {
                data.inventoryNames = java.util.Arrays.stream(invLine.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }

            return new Game(data, out);

        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar jogo: " + e.getMessage());
            return new Game("Player", out);
        }
    }

}
