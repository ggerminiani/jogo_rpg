package com.gustavo.rpg.gui;

import com.gustavo.rpg.core.Game;
import com.gustavo.rpg.core.GameOutput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;

public class RpgWindow extends JFrame {

    private final JTextArea textArea;
    private final JTextField inputField;
    private Game game;
    private boolean jogoAtivo = false;

    public RpgWindow() {
        super("RPG ***JOGER***");

        // Area de log
        textArea = new JTextArea(20, 60);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14)); // fonte monoespacada

        JScrollPane scrollPane = new JScrollPane(textArea);

        // Campo de entrada
        inputField = new JTextField();
        inputField.addActionListener(this::onCommandEntered);

        JButton enviarBtn = new JButton("Enviar");
        enviarBtn.addActionListener(this::onCommandEntered);

        // Botoes rapidos (acoes comuns)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton olharBtn = new JButton("Olhar");
        olharBtn.addActionListener(e -> executarComandoRapido("olhar"));

        JButton statusBtn = new JButton("Status");
        statusBtn.addActionListener(e -> executarComandoRapido("status"));

        JButton invBtn = new JButton("Inventario");
        invBtn.addActionListener(e -> executarComandoRapido("inventario"));

        JButton atacarBtn = new JButton("Atacar");
        atacarBtn.addActionListener(e -> executarComandoRapido("atacar"));

        JButton ajudaBtn = new JButton("Ajuda");
        ajudaBtn.addActionListener(e -> executarComandoRapido("ajuda"));

        actionsPanel.add(olharBtn);
        actionsPanel.add(statusBtn);
        actionsPanel.add(invBtn);
        actionsPanel.add(atacarBtn);
        actionsPanel.add(ajudaBtn);

        // Painel inferior: botoes + campo de texto + enviar
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(actionsPanel, BorderLayout.NORTH);
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(enviarBtn, BorderLayout.EAST);

        // Menu superior
        JMenuBar menuBar = new JMenuBar();
        JMenu jogoMenu = new JMenu("Jogo");

        JMenuItem novoItem = new JMenuItem("Novo jogo");
        novoItem.addActionListener(e -> iniciarNovoJogo());

        JMenuItem carregarItem = new JMenuItem("Carregar jogo");
        carregarItem.addActionListener(e -> carregarJogo());

        // NOVO: Salvar jogo
        JMenuItem salvarItem = new JMenuItem("Salvar jogo");
        salvarItem.addActionListener(e -> salvarJogo());

        JMenuItem sairItem = new JMenuItem("Sair");
        sairItem.addActionListener(e -> dispose());

        jogoMenu.add(novoItem);
        jogoMenu.add(carregarItem);
        jogoMenu.add(salvarItem);   // <--- entrou aqui
        jogoMenu.addSeparator();
        jogoMenu.add(sairItem);
        menuBar.add(jogoMenu);
        setJMenuBar(menuBar);


        // Layout principal
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        // Menu inicial grafico: Novo / Carregar / Sair
        SwingUtilities.invokeLater(this::mostrarMenuInicial);
    }

    // Mostra um menu inicial similar ao do console
    private void mostrarMenuInicial() {
            String[] opcoes = { "Novo jogo", "Carregar jogo", "Sair" };
            int escolha = JOptionPane.showOptionDialog(
                    this,
                    "Bem-vindo ao RPG ***JOGER***!\n\nEscolha uma opcao:",
                    "Menu inicial",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opcoes,
                    opcoes[0]
            );

            if (escolha == 0) {
                iniciarNovoJogo();
            } else if (escolha == 1) {
                carregarJogo();
            } else {
                dispose();
            }
        }

    private void iniciarNovoJogo() {
        // 1) Pergunta o nome
        String nome = JOptionPane.showInputDialog(
                this,
                "Digite o nome do jogador:",
                "Novo jogo",
                JOptionPane.QUESTION_MESSAGE
        );

        if (nome == null || nome.isBlank()) {
            return; // cancelado ou vazio
        }

        // 2) Pergunta a classe
        Object[] opcoesClasse = {"Guerreiro", "Mago", "Arqueiro"};
        Object escolha = JOptionPane.showInputDialog(
                this,
                "Escolha a classe do personagem:",
                "Classe do jogador",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoesClasse,
                opcoesClasse[0]
        );

        if (escolha == null) {
            return; // cancelado
        }

        String classeSelecionada = escolha.toString(); // "Guerreiro", "Mago" ou "Arqueiro"

        // mapeia para o texto interno usado no Game ("guerreiro", "mago", "arqueiro")
        String classeInterna = switch (classeSelecionada) {
            case "Mago" -> "mago";
            case "Arqueiro" -> "arqueiro";
            default -> "guerreiro";
        };

        // 3) Limpa tela e cria o Game com saida GUI
        textArea.setText("");
        GameOutput guiOut = new GameOutput() {
            @Override
            public void println(String s) {
                textArea.append(s + "\n");
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }

            @Override
            public void print(String s) {
                textArea.append(s);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        };

        game = new Game(nome, classeInterna, guiOut);
        jogoAtivo = true;
        game.start(); // monta o mundo e mostra as mensagens iniciais
    }

    private void salvarJogo() {
        if (game == null || !jogoAtivo) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nenhum jogo ativo para salvar.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Reaproveita a mesma lógica do console: comando "salvar"
        game.handleCommand("salvar");
    }

    private void carregarJogo() {
        JFileChooser chooser = new JFileChooser("saves");
        chooser.setDialogTitle("Escolha um arquivo de save");

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            // Se nao tem jogo ativo ainda, volta para o menu inicial
            if (game == null) {
                mostrarMenuInicial();
            }
            return;
        }

        Path arquivo = chooser.getSelectedFile().toPath();

        textArea.setText("");
        inputField.setEnabled(true);
        inputField.setText("");

        GameOutput guiOut = new GameOutput() {
            @Override
            public void println(String s) {
                textArea.append(s + "\n");
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }

            @Override
            public void print(String s) {
                textArea.append(s);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        };

        game = Game.carregarDeArquivo(arquivo, guiOut);
        jogoAtivo = true;
        game.start(); // boot + estado carregado + mensagens
        inputField.requestFocusInWindow();
    }

    private void onCommandEntered(ActionEvent e) {
        if (!jogoAtivo || game == null) {
            return;
        }
        String comando = inputField.getText().trim();
        if (comando.isEmpty()) return;

        // mostra comando digitado no log (tipo console)
        textArea.append("> " + comando + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());

        inputField.setText("");

        boolean continua = game.handleCommand(comando);
        if (!continua) {
            jogoAtivo = false;
            inputField.setEnabled(false);
            textArea.append("Jogo encerrado.\n");
        }

        inputField.requestFocusInWindow();
    }

    // Usado pelos botoes rapidos (olhar/status/inventario/atacar)
    private void executarComandoRapido(String comando) {
        if (!jogoAtivo || game == null) {
            return;
        }

        textArea.append("> " + comando + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());

        boolean continua = game.handleCommand(comando);
        if (!continua) {
            jogoAtivo = false;
            inputField.setEnabled(false);
            textArea.append("Jogo encerrado.\n");
        }

        inputField.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RpgWindow().setVisible(true));
    }
}
