package com.presentacion.analizadorSintacticoLR;

import com.persistencia.analizadorSintacticoLR.tablaLR.lr0Table;
import com.persistencia.analizadorSintacticoLR.tablaLR.lrParser;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AnalizadorLRPanel extends JPanel {

    private final JTextField txtGramPath = new JTextField();
    private final JTextField txtProgPath = new JTextField();
    private final JButton btnOpenG = new JButton("Abrir gramática");
    private final JButton btnOpenP = new JButton("Abrir programa");
    private final JButton btnAnalyze = new JButton("Analizar");
    private final JButton btnClear = new JButton("Limpiar");

    private final JTextArea txtGrammar = new JTextArea();
    private final JTextArea txtProgram = new JTextArea();

    private File grammarFile = null;
    private File programFile = null;

    public AnalizadorLRPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.weightx = 0; c.gridx = 0; c.gridy = 0; top.add(new JLabel("Seleccione una gramática aumentada (o escriba abajo):"), c);
        c.weightx = 1; c.gridx = 1; txtGramPath.setEditable(false); top.add(txtGramPath, c);
        c.weightx = 0; c.gridx = 2; top.add(btnOpenG, c);

        c.weightx = 0; c.gridx = 0; c.gridy = 1; top.add(new JLabel("Seleccione un programa (o escriba abajo):"), c);
        c.weightx = 1; c.gridx = 1; txtProgPath.setEditable(false); top.add(txtProgPath, c);
        c.weightx = 0; c.gridx = 2; top.add(btnOpenP, c);

        c.gridx = 3; JPanel rightBtns = new JPanel(new GridLayout(2,1,5,5));
        rightBtns.add(btnAnalyze); rightBtns.add(btnClear);
        top.add(rightBtns, c);

        add(top, BorderLayout.NORTH);

        txtGrammar.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        txtGrammar.setLineWrap(true);
        txtGrammar.setWrapStyleWord(true);

        txtProgram.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        txtProgram.setLineWrap(true);
        txtProgram.setWrapStyleWord(true);

        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("GRAMÁTICA (editable)"), BorderLayout.NORTH);
        left.add(new JScrollPane(txtGrammar), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("Programa fuente (editable)"), BorderLayout.NORTH);
        right.add(new JScrollPane(txtProgram), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        btnOpenG.addActionListener(this::onOpenGrammar);
        btnOpenP.addActionListener(this::onOpenProgram);
        btnAnalyze.addActionListener(this::onAnalyze);
        btnClear.addActionListener(k -> {
            grammarFile = null; programFile = null;
            txtGramPath.setText(""); txtProgPath.setText("");
            txtGrammar.setText(""); txtProgram.setText("");
        });
    }

    private void onOpenGrammar(ActionEvent e) {
        JFileChooser ch = new JFileChooser(new File(System.getProperty("user.dir")));
        ch.setAcceptAllFileFilterUsed(true);
        ch.addChoosableFileFilter(new FileNameExtensionFilter("Texto (*.txt)", "txt"));
        ch.addChoosableFileFilter(new FileNameExtensionFilter("Java (*.java)", "java"));
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            grammarFile = ch.getSelectedFile();
            txtGramPath.setText(grammarFile.getAbsolutePath());
            try {
                txtGrammar.setText(Files.readString(grammarFile.toPath()));
                txtGrammar.setCaretPosition(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo leer la gramática:\n"+ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onOpenProgram(ActionEvent e) {
        JFileChooser ch = new JFileChooser(new File(System.getProperty("user.dir")));
        ch.setAcceptAllFileFilterUsed(true);
        ch.addChoosableFileFilter(new FileNameExtensionFilter("Texto (*.txt)", "txt"));
        ch.addChoosableFileFilter(new FileNameExtensionFilter("Código (*.c, *.java, *.txt)", "c","java","txt"));
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            programFile = ch.getSelectedFile();
            txtProgPath.setText(programFile.getAbsolutePath());
            try {
                txtProgram.setText(Files.readString(programFile.toPath()));
                txtProgram.setCaretPosition(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo leer el programa:\n"+ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAnalyze(ActionEvent e) {
        try {
            String grammarPath;
            if (grammarFile != null) {
                grammarPath = grammarFile.getAbsolutePath();
            } else {
                String gtxt = txtGrammar.getText();
                if (gtxt == null || gtxt.isBlank()) {
                    JOptionPane.showMessageDialog(this, "Escribe o carga una gramática aumentada.", "Atención",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Path tempG = Files.createTempFile("grammar_lr_", ".txt");
                Files.writeString(tempG, gtxt);
                grammarPath = tempG.toAbsolutePath().toString();
            }

            lr0Table.Result table = lr0Table.buildFromFile(grammarPath);

            String prog;
            if (programFile != null) {
                prog = Files.readString(programFile.toPath());
            } else {
                prog = txtProgram.getText();
                if (prog == null) prog = "";
            }

            List<String> tokens = lrParser.lex(prog, table.terminals);
            var model = lrParser.runLRParse(grammarPath, table, tokens);

            ResultadoAnalisisLRFrame frame = new ResultadoAnalisisLRFrame(model);
            frame.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            frame.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al analizar:\n"+ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
