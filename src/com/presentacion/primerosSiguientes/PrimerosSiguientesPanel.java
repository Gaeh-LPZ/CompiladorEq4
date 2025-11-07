package com.presentacion.primerosSiguientes;

import com.persistencia.analizadorSintacticoLR.primerosYSiguientes.FirstFollow;
import com.persistencia.analizadorSintacticoLR.primerosYSiguientes.grammar;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PrimerosSiguientesPanel extends JPanel {

    private JTextField filePathField;
    private JTextArea textArea;

    public PrimerosSiguientesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblSelect = new JLabel("Seleccione una gramática:");
        filePathField = new JTextField();
        filePathField.setEditable(false);

        JButton btnOpen    = new JButton("Abrir");
        JButton btnAnalyze = new JButton("Calcular");
        JButton btnClear   = new JButton("Limpiar");

        btnOpen.addActionListener(k -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Archivos de Texto", "txt"));

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                filePathField.setText(file.getAbsolutePath());
                loadFileContent(file);
            }
        });

        btnAnalyze.addActionListener(k -> {
            String grammarText = textArea.getText().trim();
            if (grammarText.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Ingrese una gramática o abra un archivo.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                grammar g = grammar.fromText(grammarText);
                Map<String, Set<String>> first  = FirstFollow.computeFirst(g);
                Map<String, Set<String>> follow = FirstFollow.computeFollow(g, first);

                Object[][] tablaPrimeros    = toRows(first);
                Object[][] tablaSiguientes  = toRows(follow);

                ResultadosPrimerosSiguientesFrame frame = new ResultadosPrimerosSiguientesFrame(
                        tablaPrimeros,
                        tablaSiguientes
                );
                frame.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al analizar la gramática: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        btnClear.addActionListener(k -> {
            filePathField.setText("");
            textArea.setText("");
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        topPanel.add(lblSelect, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        topPanel.add(filePathField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        topPanel.add(btnOpen, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.0;
        topPanel.add(btnAnalyze, gbc);
        gbc.gridx = 2;
        topPanel.add(btnClear, gbc);

        add(topPanel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textArea.setEditable(true);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    private void loadFileContent(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            textArea.setText(sb.toString());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al leer el archivo: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Object[][] toRows(Map<String, Set<String>> map) {
        List<String> nts = new ArrayList<>(map.keySet());
        Collections.sort(nts);
        Object[][] rows = new Object[nts.size()][2];
        for (int i = 0; i < nts.size(); i++) {
            String nt = nts.get(i);
            String setStr = map.get(nt).stream()
                    .sorted()
                    .collect(Collectors.joining(", "));
            rows[i][0] = nt;
            rows[i][1] = "{" + setStr + "}";
        }
        return rows;
    }
}
