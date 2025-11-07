package com.presentacion.tablaAnalisisSintacticoLR;

import com.persistencia.analizadorSintacticoLR.tablaLR.lr0Table;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.*;

public class TablaLRPanel extends JPanel {
    private final JTextField txtPath = new JTextField();
    private final JButton btnOpen = new JButton("Abrir");
    private final JButton btnBuild = new JButton("Construir tabla");
    private final JButton btnClear = new JButton("Limpiar");
    private final JTextArea txtGrammar = new JTextArea();
    private File selectedFile = null;

    public TablaLRPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.gridx = 0; c.gridy = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        top.add(new JLabel("Seleccione una gramática aumentada:"), c);

        txtPath.setEditable(false);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        top.add(txtPath, c);

        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        top.add(btnOpen, c);

        c.gridx = 3;
        top.add(btnBuild, c);

        c.gridx = 4;
        top.add(btnClear, c);

        add(top, BorderLayout.NORTH);

        txtGrammar.setEditable(false);
        txtGrammar.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(txtGrammar), BorderLayout.CENTER);

        btnOpen.addActionListener(this::onOpen);
        btnBuild.addActionListener(this::onBuild);
        btnClear.addActionListener(unused -> {
            txtPath.setText("");
            txtGrammar.setText("");
            selectedFile = null;
        });
    }

    private void onOpen(ActionEvent e) {
        JFileChooser ch = new JFileChooser();

        File projectDir = new File(System.getProperty("user.dir"));
        ch.setCurrentDirectory(projectDir);
        
        ch.setAcceptAllFileFilterUsed(true);

        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = ch.getSelectedFile();
            txtPath.setText(selectedFile.getAbsolutePath());
            try {
                String content = com.persistencia.analizadorSintacticoLR.coleccionCanonica.grammar.readWholeFile(selectedFile.getAbsolutePath());
                txtGrammar.setText(content);
                txtGrammar.setCaretPosition(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo leer el archivo:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onBuild(ActionEvent e) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this,
                    "Primero elige un archivo de gramática aumentada (.txt o .java).",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            lr0Table.Result r = lr0Table.buildFromFile(selectedFile.getAbsolutePath());
            ResultadosTablaLRFrame frame = new ResultadosTablaLRFrame(r);
            frame.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            frame.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al construir la tabla:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
