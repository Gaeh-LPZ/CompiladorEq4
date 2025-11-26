package com.presentacion.analizadorSemanticoFinal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;

import com.persistencia.analizadorSemantico.AnalizadorSemanticoLRFinal;
import com.persistencia.analizadorSemantico.AnalizadorSemanticoLRFinal.ResultadoAnalisisCompleto;
import com.persistencia.analizadorSemantico.AnalizadorSemanticoLRFinal.TablaSimbolos;
import com.persistencia.analizadorSemantico.PasoAnalisis;
import com.persistencia.analizadorSintacticoLR.tablaLR.lr0Table;

public class analizadorSemanticoFinalPanel extends JPanel {

    private static final Color BG_DARK = new Color(0x1E1F22);
    private static final Color FG_LIGHT = new Color(0xE6E9EE);
    private static final Color BORDER_DARK = new Color(0x2A2D31);
    private static final Color BUTTON_BG = new Color(0x3574F0);
    private static final Color SUCCESS_COLOR = new Color(0x59A869);
    private static final Color ERROR_COLOR = new Color(0xE05555);

    // Componentes para cargar código fuente
    private JTextField txtCodigo;
    private JButton btnCargarCodigo;
    private JButton btnEjecutar;
    private JButton btnLimpiar;
    private JButton btnGuardarCpp;

    // Variable para almacenar el código cargado
    private String codigoFuenteCargado = "";

    private ResultadoAnalisisCompleto resultadoActual;

    public analizadorSemanticoFinalPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        add(createPanelCarga(), BorderLayout.NORTH);
    }

    private JPanel createPanelCarga() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_DARK),
                "Analizador Semántico LR - Proyecto Final",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("JetBrains Mono", Font.BOLD, 14),
                FG_LIGHT));

        // Información
        JLabel lblInfo = new JLabel(
                "<html><i>Gramática y acciones semánticas cargadas automáticamente desde pruebas/</i></html>");
        lblInfo.setForeground(new Color(0x9AA3B2));
        lblInfo.setFont(new Font("JetBrains Mono", Font.ITALIC, 11));
        lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblInfo);
        panel.add(Box.createVerticalStrut(10));

        // Fila: Código Fuente
        JPanel filaCodigo = createFilaCarga(
                "Código Fuente Java:",
                txtCodigo = new JTextField(),
                btnCargarCodigo = new JButton("📁 Cargar"));

        panel.add(filaCodigo);
        panel.add(Box.createVerticalStrut(12));

        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelBotones.setBackground(BG_DARK);

        btnEjecutar = createStyledButton("▶ Ejecutar Análisis Completo", BUTTON_BG);
        btnGuardarCpp = createStyledButton("💾 Guardar C++", SUCCESS_COLOR);
        btnLimpiar = createStyledButton("🗑 Limpiar", new Color(0x6C707E));

        btnGuardarCpp.setEnabled(false);

        panelBotones.add(btnEjecutar);
        panelBotones.add(btnGuardarCpp);
        panelBotones.add(btnLimpiar);

        panel.add(panelBotones);

        // Event Listeners
        setupEventListeners();

        return panel;
    }

    private JPanel createFilaCarga(String etiqueta, JTextField campo, JButton boton) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setBackground(BG_DARK);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel label = new JLabel(etiqueta);
        label.setForeground(FG_LIGHT);
        label.setPreferredSize(new Dimension(180, 25));

        campo.setBackground(new Color(0x2B2D30));
        campo.setForeground(FG_LIGHT);
        campo.setCaretColor(FG_LIGHT);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_DARK),
                new EmptyBorder(5, 8, 5, 8)));
        campo.setEditable(false);

        boton.setBackground(new Color(0x4A4D57));
        boton.setForeground(FG_LIGHT);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(100, 30));

        fila.add(label, BorderLayout.WEST);
        fila.add(campo, BorderLayout.CENTER);
        fila.add(boton, BorderLayout.EAST);

        return fila;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("JetBrains Mono", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(220, 35));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled())
                    btn.setBackground(bgColor.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private void setupEventListeners() {
        // Cargar Código
        btnCargarCodigo.addActionListener(e -> {
            File archivo = seleccionarArchivo("Seleccionar programa fuente Java");
            if (archivo != null) {
                txtCodigo.setText(archivo.getAbsolutePath());
                cargarCodigo(archivo);
            }
        });

        // Ejecutar Análisis
        btnEjecutar.addActionListener(e -> ejecutarAnalisis());

        // Guardar C++
        btnGuardarCpp.addActionListener(e -> guardarCodigoCpp());

        // Limpiar
        btnLimpiar.addActionListener(e -> limpiarTodo());
    }

    private File seleccionarArchivo(String titulo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(titulo);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        File carpetaPruebas = new File("pruebas");
        if (carpetaPruebas.exists() && carpetaPruebas.isDirectory()) {
            fileChooser.setCurrentDirectory(carpetaPruebas);
        } else {
            fileChooser.setCurrentDirectory(new File("."));
        }

        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    private void cargarCodigo(File archivo) {
        try {
            codigoFuenteCargado = new String(Files.readAllBytes(archivo.toPath()));
            JOptionPane.showMessageDialog(this,
                    "Código cargado exitosamente\n" +
                            "Líneas: " + codigoFuenteCargado.split("\n").length,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            mostrarError("Error al cargar código: " + ex.getMessage());
        }
    }

    private void ejecutarAnalisis() {
        if (txtCodigo.getText().isEmpty()) {
            mostrarError("Debe cargar un archivo de código fuente antes de ejecutar el análisis");
            return;
        }

        try {
            // Mostrar diálogo de progreso
            JDialog dialogProgreso = createProgressDialog();

            // Ejecutar en hilo separado
            SwingWorker<ResultadoAnalisisCompleto, Void> worker = new SwingWorker<>() {
                @Override
                protected ResultadoAnalisisCompleto doInBackground() throws Exception {
                    AnalizadorSemanticoLRFinal analizador = new AnalizadorSemanticoLRFinal();
                    // ✅ El analizador SIEMPRE retorna un resultado (nunca null)
                    return analizador.analizar(txtCodigo.getText());
                }

                @Override
                protected void done() {
                    dialogProgreso.dispose();
                    try {
                        resultadoActual = get();

                        // ✅ SIEMPRE mostrar resultados (parciales o completos)
                        if (resultadoActual != null) {
                            mostrarResultados(resultadoActual);

                            // Mensaje según resultado
                            if (resultadoActual.tieneErrores()) {
                                String mensaje = "⚠️ Análisis completado con " + resultadoActual.getErrores().size()
                                        + " error(es)\n\n";
                                mensaje += "Se muestran los resultados parciales hasta donde se pudo analizar.\n\n";
                                mensaje += "Errores:\n";
                                int maxErrores = Math.min(5, resultadoActual.getErrores().size());
                                for (int i = 0; i < maxErrores; i++) {
                                    mensaje += "  • " + resultadoActual.getErrores().get(i) + "\n";
                                }
                                if (resultadoActual.getErrores().size() > 5) {
                                    mensaje += "  ... y " + (resultadoActual.getErrores().size() - 5) + " más.";
                                }

                                JOptionPane.showMessageDialog(
                                        analizadorSemanticoFinalPanel.this,
                                        mensaje,
                                        "Análisis con Errores",
                                        JOptionPane.WARNING_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(
                                        analizadorSemanticoFinalPanel.this,
                                        "✅ Análisis completado exitosamente sin errores\n\n" +
                                                "Se abrieron las ventanas con los resultados.",
                                        "Éxito",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        } else {
                            // Esto no debería pasar ahora
                            mostrarError("No se pudo obtener ningún resultado del análisis");
                        }

                    } catch (Exception ex) {
                        // ✅ Esto solo ocurriría si hay un error MUY grave
                        System.err.println("⚠️ Excepción al obtener resultado: " + ex.getMessage());
                        ex.printStackTrace();

                        String mensaje = "Error al procesar el análisis:\n" + ex.getMessage();
                        if (resultadoActual != null) {
                            mensaje += "\n\nSe intentará mostrar resultados parciales disponibles.";
                            mostrarResultados(resultadoActual);
                        }
                        mostrarError(mensaje);
                    }
                }
            };

            worker.execute();
            dialogProgreso.setVisible(true);

        } catch (Exception ex) {
            mostrarError("Error al iniciar análisis:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private JDialog createProgressDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Analizando...", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(BG_DARK);

        JLabel label = new JLabel("Ejecutando análisis semántico...");
        label.setForeground(FG_LIGHT);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        panel.add(label, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        dialog.add(panel);
        return dialog;
    }

    private void mostrarResultados(ResultadoAnalisisCompleto resultado) {
        if (resultado == null) {
            mostrarError("No hay resultados para mostrar");
            return;
        }

        // Guardar resultado actual
        this.resultadoActual = resultado;

        try {
            // 1. VENTANA: Código Fuente (siempre disponible)
            if (!codigoFuenteCargado.isEmpty()) {
                VentanaCodigoFuente ventanaCodigo = new VentanaCodigoFuente(codigoFuenteCargado);
                ventanaCodigo.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
                ventanaCodigo.setVisible(true);
            }

            // 2. VENTANA: Tabla de Análisis Sintáctico LR
            if (resultado.getTablaLR() != null) {
                System.out.println("✅ Mostrando tabla LR...");
                VentanaTablaLR ventanaTablaLR = new VentanaTablaLR(resultado.getTablaLR());
                ventanaTablaLR.setLocation(100, 100);
                ventanaTablaLR.setVisible(true);
            } else {
                System.out.println("⚠️ Tabla LR no disponible");
            }

            // 3. VENTANA: Tira de Tokens
            if (resultado.getTokensString() != null && !resultado.getTokensString().isEmpty()) {
                Object[][] datosTokens = convertirTokensATabla(resultado.getTokensString());
                VentanaTiraTokens ventanaTokens = new VentanaTiraTokens(datosTokens);
                ventanaTokens.setLocation(150, 150);
                ventanaTokens.setVisible(true);
            } else {
                System.out.println("⚠️ Tokens no disponibles");
            }

            // 4. VENTANA: Tabla de Símbolos
            if (resultado.getTablaSimbolos() != null && resultado.getTablaSimbolos().size() > 0) {
                Object[][] datosSimbolos = new Object[resultado.getTablaSimbolos().size()][];
                int idx = 0;
                for (var s : resultado.getTablaSimbolos().getSimbolos()) {
                    datosSimbolos[idx++] = s.toArray();
                }
                VentanaTablaSimbolos ventanaSimbolos = new VentanaTablaSimbolos(datosSimbolos);
                ventanaSimbolos.setLocation(200, 200);
                ventanaSimbolos.setVisible(true);
            } else {
                System.out.println("⚠️ Tabla de símbolos vacía");
            }

            // 5. VENTANA: Tabla de Errores (SIEMPRE mostrar)
            Object[][] datosErrores;
            if (resultado.getErrores() == null || resultado.getErrores().isEmpty()) {
                datosErrores = new Object[][] { { "-", "✅ No se encontraron errores" } };
            } else {
                datosErrores = new Object[resultado.getErrores().size()][];
                for (int i = 0; i < resultado.getErrores().size(); i++) {
                    datosErrores[i] = new Object[] { i + 1, resultado.getErrores().get(i) };
                }
            }
            VentanaTablaErrores ventanaErrores = new VentanaTablaErrores(datosErrores);
            ventanaErrores.setLocation(250, 250);
            ventanaErrores.setVisible(true);

            // 6. VENTANA: Análisis Semántico LR (Corrida) - MOSTRAR PARCIAL
            if (resultado.getCorrida() != null && !resultado.getCorrida().isEmpty()) {
                Object[][] datosCorrida = new Object[resultado.getCorrida().size()][];
                for (int i = 0; i < resultado.getCorrida().size(); i++) {
                    PasoAnalisis paso = resultado.getCorrida().get(i);
                    datosCorrida[i] = paso.toArray();
                }
                VentanaCorridaSemantico ventanaCorrida = new VentanaCorridaSemantico(datosCorrida);
                ventanaCorrida.setLocation(300, 300);
                ventanaCorrida.setVisible(true);
            } else {
                System.out.println("⚠️ Corrida del análisis no disponible");
            }

            // 7. VENTANA: Código C++ Generado (si existe)
            if (resultado.getCodigoObjeto() != null && !resultado.getCodigoObjeto().isEmpty()) {
                VentanaCodigoCpp ventanaCpp = new VentanaCodigoCpp(resultado.getCodigoObjeto());
                ventanaCpp.setLocation(350, 350);
                ventanaCpp.setVisible(true);
                btnGuardarCpp.setEnabled(true);
            } else {
                System.out.println("⚠️ Código C++ no generado (análisis incompleto)");
                btnGuardarCpp.setEnabled(false);
            }

            // 8. VENTANA: Checklist
            String checklist = generarChecklistTexto(resultado);
            VentanaChecklist ventanaChecklist = new VentanaChecklist(checklist);
            ventanaChecklist.setLocation(400, 400);
            ventanaChecklist.setVisible(true);

        } catch (Exception ex) {
            System.err.println("⚠️ Error al mostrar ventanas: " + ex.getMessage());
            ex.printStackTrace();
            // No detener el proceso, algunas ventanas ya se mostraron
        }
    }

    /**
     * Convierte la lista de tokens en formato tabla
     * Formato esperado de cada token: "lexema TIPO_TOKEN numero_linea"
     * Formato salida: [#Línea, Lexema, Token]
     */
    private Object[][] convertirTokensATabla(java.util.List<String> tokens) {
        Object[][] datosTabla = new Object[tokens.size()][3];
        for (int i = 0; i < tokens.size(); i++) {
            String tokenCompleto = tokens.get(i);
            String[] partes = parsearToken(tokenCompleto);

            datosTabla[i][0] = partes[2]; // Número de línea
            datosTabla[i][1] = partes[0]; // Lexema
            datosTabla[i][2] = partes[1]; // Tipo de Token
        }
        return datosTabla;
    }

    /**
     * Parsea un token en formato: "lexema TIPO_TOKEN numero_linea"
     * Retorna: [lexema, tipo_token, numero_linea]
     */
    private String[] parsearToken(String token) {
        String[] resultado = new String[3];

        if (token == null || token.trim().isEmpty()) {
            resultado[0] = "";
            resultado[1] = "";
            resultado[2] = "0";
            return resultado;
        }

        // Dividir el token en partes
        String[] partes = token.trim().split("\\s+");

        if (partes.length >= 3) {
            // Formato estándar: "lexema TIPO linea"
            // Número de línea es el último elemento
            resultado[2] = partes[partes.length - 1];

            // Tipo de token es el penúltimo elemento
            resultado[1] = partes[partes.length - 2];

            // Lexema es todo lo anterior
            StringBuilder lexema = new StringBuilder();
            for (int i = 0; i < partes.length - 2; i++) {
                if (i > 0)
                    lexema.append(" ");
                lexema.append(partes[i]);
            }
            resultado[0] = lexema.toString();

        } else if (partes.length == 2) {
            // Formato: "TIPO linea" - el lexema está en el tipo
            resultado[0] = convertirTipoALexema(partes[0]); // AQUÍ ESTÁ EL CAMBIO
            resultado[1] = partes[0];
            resultado[2] = partes[1];
        } else if (partes.length == 1) {
            // Solo un elemento
            resultado[0] = partes[0];
            resultado[1] = partes[0];
            resultado[2] = "0";
        } else {
            resultado[0] = token;
            resultado[1] = token;
            resultado[2] = "0";
        }

        return resultado;
    }

    private String generarChecklistTexto(ResultadoAnalisisCompleto resultado) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("  CHECKLIST DE SALIDAS - ANALIZADOR SEMÁNTICO LR\n");
        sb.append("═══════════════════════════════════════════════════════\n\n");

        // Verificar cada componente
        sb.append(codigoFuenteCargado.isEmpty() ? "❌" : "✅")
                .append(" 1. Programa fuente cargado\n");

        sb.append(resultado.getTokensString() == null || resultado.getTokensString().isEmpty() ? "❌" : "✅")
                .append(" 2. Tira de tokens generada (")
                .append(resultado.getTokensString() != null ? resultado.getTokensString().size() : 0)
                .append(" tokens)\n");

        sb.append(resultado.getTablaSimbolos() == null || resultado.getTablaSimbolos().size() == 0 ? "❌" : "✅")
                .append(" 3. Tabla de símbolos construida (")
                .append(resultado.getTablaSimbolos() != null ? resultado.getTablaSimbolos().size() : 0)
                .append(" símbolos)\n");

        if (resultado.getErrores() == null || resultado.getErrores().isEmpty()) {
            sb.append("✅ 4. Tabla de errores (sin errores)\n");
        } else {
            sb.append("⚠️  4. Tabla de errores (").append(resultado.getErrores().size()).append(" error(es))\n");
        }

        sb.append(resultado.getTablaLR() == null ? "❌" : "✅")
                .append(" 5. Tabla de análisis sintáctico LR(0) generada\n");

        sb.append(resultado.getCorrida() == null || resultado.getCorrida().isEmpty() ? "⚠️ " : "✅")
                .append(" 6. Análisis semántico LR ejecutado (")
                .append(resultado.getCorrida() != null ? resultado.getCorrida().size() : 0)
                .append(" pasos)\n");

        if (resultado.getCodigoObjeto() != null && !resultado.getCodigoObjeto().isEmpty()) {
            sb.append("✅ 7. Programa objeto C++ generado\n");
        } else {
            sb.append("⚠️  7. Programa objeto C++ (no generado - análisis incompleto)\n");
        }

        sb.append("\n═══════════════════════════════════════════════════════\n");
        sb.append("  RESUMEN\n");
        sb.append("═══════════════════════════════════════════════════════\n\n");

        if (resultado.tieneErrores()) {
            sb.append("❌ Análisis completado CON ERRORES\n\n");
            sb.append("Errores encontrados:\n");
            for (String error : resultado.getErrores()) {
                sb.append("  • ").append(error).append("\n");
            }
            sb.append("\n⚠️  Se muestran resultados parciales hasta donde se detectó el error.\n");
        } else {
            sb.append("✅ Análisis completado EXITOSAMENTE sin errores\n");
        }

        return sb.toString();
    }

    private void guardarCodigoCpp() {
        if (resultadoActual == null || resultadoActual.getCodigoObjeto().isEmpty()) {
            mostrarError("No hay código C++ para guardar");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar código C++");
        fileChooser.setSelectedFile(new File("programa.cpp"));

        int resultado = fileChooser.showSaveDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(archivo)) {
                writer.write(resultadoActual.getCodigoObjeto());
                JOptionPane.showMessageDialog(this,
                        "Código C++ guardado exitosamente en:\n" + archivo.getAbsolutePath(),
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                mostrarError("Error al guardar archivo:\n" + ex.getMessage());
            }
        }
    }

    private void limpiarTodo() {
        txtCodigo.setText("");
        codigoFuenteCargado = "";
        btnGuardarCpp.setEnabled(false);
        resultadoActual = null;

        JOptionPane.showMessageDialog(this,
                "Todos los datos han sido limpiados",
                "Limpieza completada",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // ==================== CLASE: VentanaCodigoFuente ====================
    private static class VentanaCodigoFuente extends JFrame {
        public VentanaCodigoFuente(String codigo) {
            super("1️⃣ Código Fuente");
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JTextArea area = new JTextArea(codigo);
            area.setEditable(false);
            area.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
            area.setBackground(new Color(0x2B2D30));
            area.setForeground(new Color(0xE6E9EE));
            area.setCaretColor(new Color(0xE6E9EE));

            add(new JScrollPane(area));
        }
    }

    // ==================== CLASE: VentanaTiraTokens (TABLA) ====================
    private static class VentanaTiraTokens extends JFrame {
        public VentanaTiraTokens(Object[][] datos) {
            super("2️⃣ Tira de Tokens");
            setSize(700, 500);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            String[] columnas = { "#Línea", "Lexema", "Token" };
            DefaultTableModel modelo = new DefaultTableModel(datos, columnas) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable tabla = new JTable(modelo);
            tabla.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
            tabla.setBackground(new Color(0x2B2D30));
            tabla.setForeground(new Color(0xE6E9EE));
            tabla.setGridColor(new Color(0x2A2D31));
            tabla.setRowHeight(25);
            tabla.getTableHeader().setBackground(new Color(0x3C3F41));
            tabla.getTableHeader().setForeground(new Color(0xE6E9EE));
            tabla.getTableHeader().setFont(new Font("JetBrains Mono", Font.BOLD, 12));

            // Ajustar anchos de columna
            tabla.getColumnModel().getColumn(0).setPreferredWidth(80); // #Línea
            tabla.getColumnModel().getColumn(1).setPreferredWidth(200); // Lexema
            tabla.getColumnModel().getColumn(2).setPreferredWidth(250); // Token

            add(new JScrollPane(tabla));
        }
    }

    // ==================== CLASE: VentanaTablaSimbolos ====================
    private static class VentanaTablaSimbolos extends JFrame {
        public VentanaTablaSimbolos(Object[][] datos) {
            super("3️⃣ Tabla de Símbolos");
            setSize(700, 500);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            String[] columnas = { "ID", "Nombre", "Tipo", "Valor", "Línea" };
            JTable tabla = new JTable(datos, columnas);
            tabla.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
            tabla.setBackground(new Color(0x2B2D30));
            tabla.setForeground(new Color(0xE6E9EE));
            tabla.setGridColor(new Color(0x2A2D31));
            tabla.setRowHeight(25);
            tabla.getTableHeader().setBackground(new Color(0x3C3F41));
            tabla.getTableHeader().setForeground(new Color(0xE6E9EE));

            add(new JScrollPane(tabla));
        }
    }

    // ==================== CLASE: VentanaTablaErrores ====================
    private static class VentanaTablaErrores extends JFrame {
        public VentanaTablaErrores(Object[][] datos) {
            super("4️⃣ Tabla de Errores");
            setSize(800, 400);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            String[] columnas = { "#", "Descripción del Error" };
            JTable tabla = new JTable(datos, columnas);
            tabla.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
            tabla.setBackground(new Color(0x2B2D30));
            tabla.setForeground(new Color(0xE6E9EE));
            tabla.setGridColor(new Color(0x2A2D31));
            tabla.setRowHeight(25);
            tabla.getTableHeader().setBackground(new Color(0x3C3F41));
            tabla.getTableHeader().setForeground(new Color(0xE6E9EE));

            add(new JScrollPane(tabla));
        }
    }

    // ==================== CLASE: VentanaCorridaSemantico ====================
    private static class VentanaCorridaSemantico extends JFrame {
        public VentanaCorridaSemantico(Object[][] datos) {
            super("5️⃣ Análisis Semántico LR");
            setSize(1200, 700);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            String[] columnas = { "Paso", "Pila", "Entrada", "Acción", "Salida" };
            JTable tabla = new JTable(datos, columnas);
            tabla.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
            tabla.setBackground(new Color(0x2B2D30));
            tabla.setForeground(new Color(0xE6E9EE));
            tabla.setGridColor(new Color(0x2A2D31));
            tabla.setRowHeight(25);
            tabla.getTableHeader().setBackground(new Color(0x3C3F41));
            tabla.getTableHeader().setForeground(new Color(0xE6E9EE));

            // Ajustar anchos de columnas
            tabla.getColumnModel().getColumn(0).setPreferredWidth(60);
            tabla.getColumnModel().getColumn(1).setPreferredWidth(200);
            tabla.getColumnModel().getColumn(2).setPreferredWidth(200);
            tabla.getColumnModel().getColumn(3).setPreferredWidth(180);
            tabla.getColumnModel().getColumn(4).setPreferredWidth(250);

            add(new JScrollPane(tabla));
        }
    }

    // ==================== CLASE: VentanaCodigoCpp ====================
    private static class VentanaCodigoCpp extends JFrame {
        public VentanaCodigoCpp(String codigo) {
            super("6️⃣ Código Objeto (C++)");
            setSize(900, 600);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JTextArea area = new JTextArea(codigo);
            area.setEditable(false);
            area.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
            area.setBackground(new Color(0x2B2D30));
            area.setForeground(new Color(0xE6E9EE));
            area.setCaretColor(new Color(0xE6E9EE));

            add(new JScrollPane(area));
        }
    }

    // ==================== CLASE: VentanaChecklist ====================
    private static class VentanaChecklist extends JFrame {
        public VentanaChecklist(String texto) {
            super("7️⃣ Checklist");
            setSize(700, 600);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JTextArea area = new JTextArea(texto);
            area.setEditable(false);
            area.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
            area.setBackground(new Color(0x2B2D30));
            area.setForeground(new Color(0xE6E9EE));

            add(new JScrollPane(area));
        }
    }

    /**
     * Convierte el tipo de token a su lexema correspondiente
     */
    private String convertirTipoALexema(String tipo) {
        switch (tipo) {
            case "PUNTO_Y_COMA":
                return ";";
            case "COMA":
                return ",";
            case "PUNTO":
                return ".";
            case "DOS_PUNTOS":
                return ":";
            case "PARENTESIS_IZQ":
                return "(";
            case "PARENTESIS_DER":
                return ")";
            case "LLAVE_IZQ":
                return "{";
            case "LLAVE_DER":
                return "}";
            case "CORCHETE_IZQ":
                return "[";
            case "CORCHETE_DER":
                return "]";
            case "SUMA":
                return "+";
            case "RESTA":
                return "-";
            case "MULTIPLICACION":
                return "*";
            case "DIVISION":
                return "/";
            case "MODULO":
                return "%";
            case "ASIGNACION":
                return "=";
            case "IGUAL":
                return "==";
            case "DIFERENTE":
                return "!=";
            case "MENOR":
                return "<";
            case "MAYOR":
                return ">";
            case "MENOR_IGUAL":
                return "<=";
            case "MAYOR_IGUAL":
                return ">=";
            case "AND":
                return "&&";
            case "OR":
                return "||";
            case "NOT":
                return "!";
            case "INCREMENTO":
                return "++";
            case "DECREMENTO":
                return "--";
            default:
                return tipo; // Si no es un símbolo conocido, devolver el tipo
        }
    }

    // ==================== CLASE: VentanaTablaLR ====================
    private static class VentanaTablaLR extends JFrame {
        public VentanaTablaLR(lr0Table.Result tablaLR) {
            super("📊 Tabla de Análisis Sintáctico LR(0)");
            setSize(1400, 800);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBackground(new Color(0x2B2D30));
            mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

            // Información superior
            JLabel lblInfo = new JLabel(
                    "ACTION: d# = desplazar, r# = reducir, acep = aceptar | GOTO: números de estado");
            lblInfo.setForeground(new Color(0x9AA3B2));
            lblInfo.setFont(new Font("JetBrains Mono", Font.ITALIC, 11));
            lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
            mainPanel.add(lblInfo, BorderLayout.NORTH);

            // Construir datos de la tabla
            Object[][] datos = construirDatosTablaLR(tablaLR);
            String[] columnas = construirColumnasTablaLR(tablaLR);

            DefaultTableModel modelo = new DefaultTableModel(datos, columnas) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable tabla = new JTable(modelo);
            tabla.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
            tabla.setBackground(new Color(0x2B2D30));
            tabla.setForeground(new Color(0xE6E9EE));
            tabla.setGridColor(new Color(0x2A2D31));
            tabla.setRowHeight(25);
            tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            // Encabezados
            tabla.getTableHeader().setBackground(new Color(0x3C3F41));
            tabla.getTableHeader().setForeground(new Color(0xE6E9EE));
            tabla.getTableHeader().setFont(new Font("JetBrains Mono", Font.BOLD, 11));

            // Ajustar anchos de columnas
            tabla.getColumnModel().getColumn(0).setPreferredWidth(80); // Estado
            for (int i = 1; i < columnas.length; i++) {
                tabla.getColumnModel().getColumn(i).setPreferredWidth(80);
            }

            // Renderizador personalizado para colorear celdas
            tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    setFont(new Font("JetBrains Mono", Font.PLAIN, 10));

                    if (column == 0) {
                        // Columna de estado
                        c.setBackground(new Color(0x3C3F41));
                        c.setForeground(new Color(0xE6E9EE));
                        setHorizontalAlignment(CENTER);
                    } else if (value != null && !value.toString().isEmpty()) {
                        String val = value.toString();
                        if (val.startsWith("d")) {
                            // Desplazamiento - azul
                            c.setBackground(new Color(0x2D4A6B));
                            c.setForeground(Color.WHITE);
                        } else if (val.startsWith("r")) {
                            // Reducción - verde
                            c.setBackground(new Color(0x2D4D2D));
                            c.setForeground(Color.WHITE);
                        } else if (val.equalsIgnoreCase("acep") || val.equalsIgnoreCase("aceptar")) {
                            // Aceptar - dorado
                            c.setBackground(new Color(0x6B5A2D));
                            c.setForeground(Color.WHITE);
                        } else if (val.matches("\\d+")) {
                            // GOTO - púrpura claro
                            c.setBackground(new Color(0x4D3D5A));
                            c.setForeground(Color.WHITE);
                        } else {
                            c.setBackground(new Color(0x2B2D30));
                            c.setForeground(new Color(0xE6E9EE));
                        }
                        setHorizontalAlignment(CENTER);
                    } else {
                        c.setBackground(new Color(0x2B2D30));
                        c.setForeground(new Color(0x5C5C5C));
                        setHorizontalAlignment(CENTER);
                    }

                    return c;
                }
            });

            JScrollPane scrollPane = new JScrollPane(tabla);
            scrollPane.getViewport().setBackground(new Color(0x2B2D30));
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            add(mainPanel);
        }

        /**
         * Construir datos de la tabla LR
         */
        private Object[][] construirDatosTablaLR(lr0Table.Result tablaLR) {
            int numEstados = tablaLR.action.size();

            // Obtener todos los símbolos (terminales y no terminales)
            Set<String> terminales = new TreeSet<>();
            Set<String> noTerminales = new TreeSet<>();

            for (Map<String, String> actionMap : tablaLR.action.values()) {
                terminales.addAll(actionMap.keySet());
            }

            for (Map<String, Integer> gotoMap : tablaLR.gotoTable.values()) {
                noTerminales.addAll(gotoMap.keySet());
            }

            // Crear matriz de datos
            Object[][] datos = new Object[numEstados][1 + terminales.size() + noTerminales.size()];

            for (int estado = 0; estado < numEstados; estado++) {
                int col = 0;

                // Columna de estado
                datos[estado][col++] = estado;

                // Columnas ACTION (terminales)
                Map<String, String> actionMap = tablaLR.action.getOrDefault(estado, new HashMap<>());
                for (String terminal : terminales) {
                    datos[estado][col++] = actionMap.getOrDefault(terminal, "");
                }

                // Columnas GOTO (no terminales)
                Map<String, Integer> gotoMap = tablaLR.gotoTable.getOrDefault(estado, new HashMap<>());
                for (String noTerminal : noTerminales) {
                    Integer gotoEstado = gotoMap.get(noTerminal);
                    datos[estado][col++] = (gotoEstado != null) ? gotoEstado.toString() : "";
                }
            }

            return datos;
        }

        /**
         * Construir nombres de columnas
         */
        private String[] construirColumnasTablaLR(lr0Table.Result tablaLR) {
            Set<String> terminales = new TreeSet<>();
            Set<String> noTerminales = new TreeSet<>();

            for (Map<String, String> actionMap : tablaLR.action.values()) {
                terminales.addAll(actionMap.keySet());
            }

            for (Map<String, Integer> gotoMap : tablaLR.gotoTable.values()) {
                noTerminales.addAll(gotoMap.keySet());
            }

            List<String> columnas = new ArrayList<>();
            columnas.add("Estado");

            // ACTION
            for (String terminal : terminales) {
                columnas.add(terminal);
            }

            // GOTO
            for (String noTerminal : noTerminales) {
                columnas.add(noTerminal);
            }

            return columnas.toArray(new String[0]);
        }
    }
}