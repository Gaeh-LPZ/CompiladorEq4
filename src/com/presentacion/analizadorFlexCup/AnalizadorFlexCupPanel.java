package com.presentacion.analizadorFlexCup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

/**
 * Panel para el Analizador Flex-Cup - Proyecto Final
 * Integra análisis léxico (Flex) y sintáctico-semántico (Cup)
 */
public class AnalizadorFlexCupPanel extends JPanel {

    private static final Color BG_DARK = new Color(0x1E1F22);
    private static final Color BG_DARKER = new Color(0x2B2D30);
    private static final Color FG_LIGHT = new Color(0xE6E9EE);
    private static final Color ACCENT_COLOR = new Color(0x3574F0);
    private static final Color SUCCESS_COLOR = new Color(0x499C54);
    private static final Color ERROR_COLOR = new Color(0xC55252);
    private static final Color BORDER_DARK = new Color(0x2A2D31);

    // Directorio por defecto para archivos
    private static final String DIRECTORIO_ARCHIVOS = "pruebas/final_flex";

    // Componentes principales
    private JTextArea areaCodigoFuente;
    private JTextArea areaArchivoFlex;
    private JTextArea areaArchivoCup;
    private JTextArea areaResultadoCompilacion;

    // Referencias a ventanas de resultados (para poder cerrarlas si están abiertas)
    private JFrame ventanaTokens;
    private JFrame ventanaSimbolos;
    private JFrame ventanaTraduccion;
    private JFrame ventanaErrores;

    private JLabel labelEstado;
    private File archivoFuenteActual;

    // Controlador para JFlex-CUP
    private JFlexCupController controlador;

    public AnalizadorFlexCupPanel() {
        this.controlador = new JFlexCupController();
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        // Panel superior: Barra de herramientas
        add(crearBarraHerramientas(), BorderLayout.NORTH);

        // Panel central: Contenido principal con pestañas SIMPLIFICADAS
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_DARK);
        tabbedPane.setForeground(FG_LIGHT);

        // Solo mantenemos las pestañas de entrada
        tabbedPane.addTab("📁 Archivos", crearPanelArchivos());
        tabbedPane.addTab("🔨 Compilación", crearPanelCompilacion());

        add(tabbedPane, BorderLayout.CENTER);

        // Panel inferior: Barra de estado
        add(crearBarraEstado(), BorderLayout.SOUTH);
    }

    // ==================== BARRA DE HERRAMIENTAS ====================
    private JPanel crearBarraHerramientas() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(BG_DARKER);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x3E4047)));

        // Botón: Abrir archivo JFlex (.flex)
        JButton btnAbrirFlex = crearBoton("📄 Abrir JFlex (.flex)", ACCENT_COLOR);
        btnAbrirFlex.addActionListener(e -> abrirArchivoJFlex());
        panel.add(btnAbrirFlex);

        // Botón: Abrir archivo CUP (.cup)
        JButton btnAbrirCup = crearBoton("📄 Abrir CUP (.cup)", ACCENT_COLOR);
        btnAbrirCup.addActionListener(e -> abrirArchivoCup());
        panel.add(btnAbrirCup);

        panel.add(Box.createRigidArea(new Dimension(20, 0)));

        // Botón: Compilar JFlex-CUP
        JButton btnCompilar = crearBoton("🔨 Compilar JFlex-CUP", new Color(0xF09035));
        btnCompilar.addActionListener(e -> compilarJFlexCup());
        panel.add(btnCompilar);

        panel.add(Box.createRigidArea(new Dimension(20, 0)));

        // Botón: Abrir programa a analizar
        JButton btnAbrirPrograma = crearBoton("📂 Abrir Programa", ACCENT_COLOR);
        btnAbrirPrograma.addActionListener(e -> abrirProgramaFuente());
        panel.add(btnAbrirPrograma);

        // Botón: Analizar
        JButton btnAnalizar = crearBoton("▶️ Analizar", SUCCESS_COLOR);
        btnAnalizar.addActionListener(e -> analizarPrograma());
        panel.add(btnAnalizar);

        // Botones para mostrar resultados en ventanas separadas
        panel.add(Box.createRigidArea(new Dimension(20, 0)));

        JButton btnVerTokens = crearBoton("🎯 Ver Tokens", new Color(0x9B59B6));
        btnVerTokens.addActionListener(e -> mostrarVentanaTokens());
        panel.add(btnVerTokens);

        JButton btnVerSimbolos = crearBoton("📚 Ver Símbolos", new Color(0x3498DB));
        btnVerSimbolos.addActionListener(e -> mostrarVentanaSimbolos());
        panel.add(btnVerSimbolos);

        JButton btnVerTraduccion = crearBoton("🔄 Ver Traducción", new Color(0x2ECC71));
        btnVerTraduccion.addActionListener(e -> mostrarVentanaTraduccion());
        panel.add(btnVerTraduccion);

        JButton btnVerErrores = crearBoton("❌ Ver Errores", ERROR_COLOR);
        btnVerErrores.addActionListener(e -> mostrarVentanaErrores());
        panel.add(btnVerErrores);

        panel.add(Box.createRigidArea(new Dimension(20, 0)));

        // Botón: Limpiar todo
        JButton btnLimpiar = crearBoton("🗑️ Limpiar", new Color(0xE74C3C));
        btnLimpiar.addActionListener(e -> limpiarTodo());
        panel.add(btnLimpiar);

        return panel;
    }

    // ==================== PANEL ARCHIVOS ====================
    private JPanel crearPanelArchivos() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Archivo JFlex
        JPanel panelJFlex = crearPanelEditor("Archivo JFlex (.flex)", areaArchivoFlex = crearTextArea());
        panel.add(panelJFlex);

        // Archivo CUP
        JPanel panelCup = crearPanelEditor("Archivo CUP (.cup)", areaArchivoCup = crearTextArea());
        panel.add(panelCup);

        // Código fuente a analizar
        JPanel panelFuente = crearPanelEditor("Programa Fuente", areaCodigoFuente = crearTextArea());
        panel.add(panelFuente);

        return panel;
    }

    // ==================== PANEL COMPILACIÓN ====================
    private JPanel crearPanelCompilacion() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Resultado de compilación
        JPanel panelCompilacion = crearPanelConScroll(
                "📋 Resultado de Compilación JFlex-CUP",
                areaResultadoCompilacion = crearTextArea());

        panel.add(panelCompilacion, BorderLayout.CENTER);

        return panel;
    }

    // ==================== BARRA DE ESTADO ====================
    private JPanel crearBarraEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARKER);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x3E4047)));

        labelEstado = new JLabel("  📌 Listo - Cargue archivos JFlex y CUP para comenzar");
        labelEstado.setForeground(FG_LIGHT);
        labelEstado.setBorder(new EmptyBorder(6, 8, 6, 8));
        panel.add(labelEstado, BorderLayout.WEST);

        return panel;
    }

    // ==================== MÉTODOS DE ACCIÓN ====================

    private void abrirArchivoJFlex() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(DIRECTORIO_ARCHIVOS));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos JFlex (*.flex, *.jflex, *.l)", "flex", "jflex", "l"));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivoFlex = fc.getSelectedFile();
            cargarArchivoEnArea(archivoFlex, areaArchivoFlex);
            actualizarEstado("✅ Archivo JFlex cargado: " + archivoFlex.getName());
        }
    }

    private void abrirArchivoCup() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(DIRECTORIO_ARCHIVOS));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos CUP (*.cup)", "cup"));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivoCup = fc.getSelectedFile();
            cargarArchivoEnArea(archivoCup, areaArchivoCup);
            actualizarEstado("✅ Archivo CUP cargado: " + archivoCup.getName());
        }
    }

    private void abrirProgramaFuente() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(DIRECTORIO_ARCHIVOS));

        // Permitir archivos .java y .txt
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos de código (*.java, *.txt)", "java", "txt"));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            archivoFuenteActual = fc.getSelectedFile();
            cargarArchivoEnArea(archivoFuenteActual, areaCodigoFuente);
            actualizarEstado("✅ Programa fuente cargado: " + archivoFuenteActual.getName());
        }
    }

    private void compilarJFlexCup() {
        // Verificar JARs de CUP
        if (!controlador.verificarArchivos()) {
            JOptionPane.showMessageDialog(this,
                    "Faltan archivos necesarios:\n" +
                            "- lib/java-cup-11b.jar\n" +
                            "- lib/java-cup-11b-runtime.jar\n\n" +
                            "Descárgalos de: http://www2.cs.tum.edu/projects/cup/",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar que se haya cargado contenido CUP
        String contenidoCup = areaArchivoCup.getText().trim();
        if (contenidoCup.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe cargar el contenido del archivo CUP (.cup) primero",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        areaResultadoCompilacion.setText("🔨 Iniciando compilación JFlex-CUP...\n\n");
        actualizarEstado("⏳ Compilando...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Obtener contenido de las áreas de texto
                String contenidoJFlex = areaArchivoFlex.getText().trim();
                String contenidoCup = areaArchivoCup.getText().trim();

                // Directorio de trabajo temporal
                String directorioTrabajo = DIRECTORIO_ARCHIVOS;

                // Compilar usando el contenido directamente
                return controlador.compilarJFlexCup(contenidoJFlex, contenidoCup, directorioTrabajo);
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    areaResultadoCompilacion.setText(controlador.getLogCompilacion());

                    if (exito) {
                        actualizarEstado("✅ Compilación exitosa - Listo para analizar");
                        JOptionPane.showMessageDialog(
                                AnalizadorFlexCupPanel.this,
                                "Compilación exitosa\n\nEl analizador está listo para usarse.",
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        actualizarEstado("❌ Error en la compilación");
                        JOptionPane.showMessageDialog(
                                AnalizadorFlexCupPanel.this,
                                "Error durante la compilación.\nRevise el log para más detalles.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    actualizarEstado("❌ Error inesperado");
                    JOptionPane.showMessageDialog(
                            AnalizadorFlexCupPanel.this,
                            "Error inesperado: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void analizarPrograma() {
        if (areaCodigoFuente.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor cargue un programa fuente para analizar",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        actualizarEstado("⏳ Analizando programa...");

        SwingWorker<ResultadoAnalisis, Void> worker = new SwingWorker<>() {
            @Override
            protected ResultadoAnalisis doInBackground() throws Exception {
                // Analizar directamente el contenido del área de texto
                return controlador.analizarPrograma(areaCodigoFuente.getText());
            }

            @Override
            protected void done() {
                try {
                    ResultadoAnalisis resultado = get();

                    if (resultado.tieneError()) {
                        actualizarEstado("❌ Error en el análisis");
                        JOptionPane.showMessageDialog(
                                AnalizadorFlexCupPanel.this,
                                resultado.getError(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Guardar resultados en el controlador para mostrarlos luego
                    controlador.setResultadosAnalisis(resultado);
                    actualizarEstado("✅ Análisis completado - Use los botones para ver resultados");

                    // Preguntar si quiere ver algún resultado inmediatamente
                    int opcion = JOptionPane.showOptionDialog(
                            AnalizadorFlexCupPanel.this,
                            "Análisis completado exitosamente.\n¿Qué resultado desea ver?",
                            "Resultados Disponibles",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new String[] { "Ver Tokens", "Ver Símbolos", "Ver Traducción", "Ver Errores", "Más tarde" },
                            "Más tarde");

                    switch (opcion) {
                        case 0:
                            mostrarVentanaTokens();
                            break;
                        case 1:
                            mostrarVentanaSimbolos();
                            break;
                        case 2:
                            mostrarVentanaTraduccion();
                            break;
                        case 3:
                            mostrarVentanaErrores();
                            break;
                    }

                } catch (Exception e) {
                    actualizarEstado("❌ Error inesperado");
                    JOptionPane.showMessageDialog(
                            AnalizadorFlexCupPanel.this,
                            "Error: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    // ==================== MÉTODOS PARA MOSTRAR VENTANAS MEJORADAS
    // ====================

    private void mostrarVentanaTokens() {
        if (ventanaTokens != null) {
            ventanaTokens.dispose();
        }

        ventanaTokens = crearVentanaResultado(
                "🎯 Tira de Tokens",
                900,
                600,
                controlador.getResultadosAnalisis().getTokens(),
                new Color(0x9B59B6));
    }

    private void mostrarVentanaSimbolos() {
        if (ventanaSimbolos != null) {
            ventanaSimbolos.dispose();
        }

        String contenido = controlador.getResultadosAnalisis().getSimbolos();
        if (contenido != null && !contenido.trim().isEmpty()) {
            ventanaSimbolos = crearVentanaTabla(
                    "📚 Tabla de Símbolos",
                    800,
                    500,
                    contenido,
                    new Color(0x3498DB));
        } else {
            JOptionPane.showMessageDialog(this,
                    "No hay símbolos para mostrar",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void mostrarVentanaTraduccion() {
        if (ventanaTraduccion != null) {
            ventanaTraduccion.dispose();
        }

        ventanaTraduccion = crearVentanaResultado(
                "🔄 Traducción (Código Objeto)",
                900,
                600,
                controlador.getResultadosAnalisis().getTraduccion(),
                new Color(0x2ECC71));
    }

    private void mostrarVentanaErrores() {
        if (ventanaErrores != null) {
            ventanaErrores.dispose();
        }

        String contenido = controlador.getResultadosAnalisis().getErrores();
        if (contenido != null && !contenido.trim().isEmpty()) {
            ventanaErrores = crearVentanaTabla(
                    "❌ Tabla de Errores",
                    800,
                    400,
                    contenido,
                    ERROR_COLOR);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No hay errores para mostrar",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ==================== MÉTODOS PARA CREAR VENTANAS ELEGANTES
    // ====================

    private JFrame crearVentanaResultado(String titulo, int ancho, int alto, String contenido, Color colorAccent) {
        JFrame ventana = new JFrame(titulo);
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setSize(ancho, alto);
        ventana.setLocationRelativeTo(this);

        // Panel principal con diseño moderno
        JPanel panelPrincipal = new JPanel(new BorderLayout(0, 0));
        panelPrincipal.setBackground(BG_DARK);

        // Barra superior con título y botones
        JPanel barraTitulo = crearBarraTitulo(titulo, colorAccent, ventana);
        panelPrincipal.add(barraTitulo, BorderLayout.NORTH);

        // Área de contenido
        JTextArea areaTexto = crearTextArea();
        areaTexto.setText(contenido != null ? contenido : "No hay datos disponibles");
        areaTexto.setCaretPosition(0);
        areaTexto.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(areaTexto);
        scrollPane.setBackground(BG_DARKER);
        scrollPane.getViewport().setBackground(BG_DARKER);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_DARK));

        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // Barra inferior con estadísticas
        JPanel barraInferior = crearBarraInferior(contenido);
        panelPrincipal.add(barraInferior, BorderLayout.SOUTH);

        ventana.add(panelPrincipal);
        ventana.setVisible(true);
        return ventana;
    }

    private JFrame crearVentanaTabla(String titulo, int ancho, int alto, String contenido, Color colorAccent) {
        JFrame ventana = new JFrame(titulo);
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setSize(ancho, alto);
        ventana.setLocationRelativeTo(this);

        // Panel principal con diseño moderno
        JPanel panelPrincipal = new JPanel(new BorderLayout(0, 0));
        panelPrincipal.setBackground(BG_DARK);

        // Barra superior con título y botones
        JPanel barraTitulo = crearBarraTitulo(titulo, colorAccent, ventana);
        panelPrincipal.add(barraTitulo, BorderLayout.NORTH);

        // Crear tabla desde contenido
        String[] lineas = contenido.split("\n");
        if (lineas.length > 0) {
            // Obtener encabezados de la primera línea
            String[] encabezados = lineas[0].split("\t");
            DefaultTableModel modelo = new DefaultTableModel(encabezados, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            // Agregar datos
            for (int i = 1; i < lineas.length; i++) {
                String[] fila = lineas[i].split("\t");
                if (fila.length == encabezados.length) {
                    modelo.addRow(fila);
                }
            }

            JTable tabla = crearTablaEstilizada(modelo);
            JScrollPane scrollPane = new JScrollPane(tabla);
            scrollPane.setBackground(BG_DARKER);
            scrollPane.getViewport().setBackground(BG_DARKER);
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_DARK));

            panelPrincipal.add(scrollPane, BorderLayout.CENTER);

            // Barra inferior con estadísticas
            JPanel barraInferior = crearBarraInferiorTabla(modelo);
            panelPrincipal.add(barraInferior, BorderLayout.SOUTH);
        }

        ventana.add(panelPrincipal);
        ventana.setVisible(true);
        return ventana;
    }

    private JPanel crearBarraTitulo(String titulo, Color colorAccent, JFrame ventana) {
        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setBackground(colorAccent);
        barra.setBorder(new EmptyBorder(12, 15, 12, 15));

        // Título
        JLabel labelTitulo = new JLabel(titulo);
        labelTitulo.setForeground(Color.WHITE);
        labelTitulo.setFont(new Font("JetBrains Mono", Font.BOLD, 14));
        barra.add(labelTitulo, BorderLayout.WEST);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBotones.setOpaque(false);

        // Botón copiar
        JButton btnCopiar = crearBotonVentana("📋 Copiar");
        btnCopiar.addActionListener(e -> {
            Component component = ((JPanel) ventana.getContentPane().getComponent(0))
                    .getComponent(1);
            if (component instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) component;
                Component view = scroll.getViewport().getView();
                if (view instanceof JTextArea) {
                    JTextArea area = (JTextArea) view;
                    area.selectAll();
                    area.copy();
                    mostrarNotificacion(ventana, "✅ Contenido copiado");
                }
            }
        });

        // Botón exportar
        JButton btnExportar = crearBotonVentana("💾 Exportar");
        btnExportar.addActionListener(e -> exportarContenidoVentana(ventana, titulo));

        // Botón cerrar
        JButton btnCerrar = crearBotonVentana("✕");
        btnCerrar.addActionListener(e -> ventana.dispose());

        panelBotones.add(btnCopiar);
        panelBotones.add(btnExportar);
        panelBotones.add(btnCerrar);

        barra.add(panelBotones, BorderLayout.EAST);

        return barra;
    }

    private JPanel crearBarraInferior(String contenido) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        barra.setBackground(BG_DARKER);
        barra.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_DARK));

        // Contar líneas
        int lineas = contenido != null ? contenido.split("\n").length : 0;
        int caracteres = contenido != null ? contenido.length() : 0;

        JLabel labelInfo = new JLabel(String.format("📊 %d líneas  •  %,d caracteres", lineas, caracteres));
        labelInfo.setForeground(new Color(0x9B9B9B));
        labelInfo.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));

        barra.add(labelInfo);

        return barra;
    }

    private JPanel crearBarraInferiorTabla(DefaultTableModel modelo) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        barra.setBackground(BG_DARKER);
        barra.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_DARK));

        JLabel labelInfo = new JLabel(String.format("📊 %d filas  •  %d columnas",
                modelo.getRowCount(), modelo.getColumnCount()));
        labelInfo.setForeground(new Color(0x9B9B9B));
        labelInfo.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));

        barra.add(labelInfo);

        return barra;
    }

    private JButton crearBotonVentana(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(255, 255, 255, 30));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("JetBrains Mono", Font.BOLD, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(texto.length() > 3 ? 100 : 30, 28));

        // Efecto hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = btn.getBackground();

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(255, 255, 255, 50));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(originalColor);
            }
        });

        return btn;
    }

    private void exportarContenidoVentana(JFrame ventana, String nombreArchivo) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(nombreArchivo.replace(" ", "_") + ".txt"));

        if (fc.showSaveDialog(ventana) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
                Component component = ((JPanel) ventana.getContentPane().getComponent(0))
                        .getComponent(1);

                if (component instanceof JScrollPane) {
                    JScrollPane scroll = (JScrollPane) component;
                    Component view = scroll.getViewport().getView();

                    if (view instanceof JTextArea) {
                        JTextArea area = (JTextArea) view;
                        writer.write(area.getText());
                    } else if (view instanceof JTable) {
                        JTable tabla = (JTable) view;
                        exportarTablaArchivo(tabla, writer);
                    }
                }

                mostrarNotificacion(ventana, "✅ Archivo exportado exitosamente");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(ventana,
                        "Error al exportar: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarNotificacion(JFrame ventana, String mensaje) {
        JLabel notificacion = new JLabel(mensaje);
        notificacion.setForeground(Color.WHITE);
        notificacion.setBackground(new Color(0, 0, 0, 180));
        notificacion.setOpaque(true);
        notificacion.setBorder(new EmptyBorder(10, 15, 10, 15));
        notificacion.setFont(new Font("JetBrains Mono", Font.BOLD, 12));

        JWindow popup = new JWindow(ventana);
        popup.add(notificacion);
        popup.pack();
        popup.setLocationRelativeTo(ventana);
        popup.setVisible(true);

        Timer timer = new Timer(2000, e -> popup.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private void exportarTablaArchivo(JTable tabla, PrintWriter writer) {
        // Escribir encabezados
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            writer.print(tabla.getColumnName(i));
            if (i < tabla.getColumnCount() - 1)
                writer.print("\t");
        }
        writer.println();

        // Escribir datos
        for (int i = 0; i < tabla.getRowCount(); i++) {
            for (int j = 0; j < tabla.getColumnCount(); j++) {
                Object valor = tabla.getValueAt(i, j);
                writer.print(valor != null ? valor : "");
                if (j < tabla.getColumnCount() - 1)
                    writer.print("\t");
            }
            writer.println();
        }
    }

    private void limpiarTodo() {
        // Cerrar todas las ventanas abiertas
        if (ventanaTokens != null) {
            ventanaTokens.dispose();
            ventanaTokens = null;
        }
        if (ventanaSimbolos != null) {
            ventanaSimbolos.dispose();
            ventanaSimbolos = null;
        }
        if (ventanaTraduccion != null) {
            ventanaTraduccion.dispose();
            ventanaTraduccion = null;
        }
        if (ventanaErrores != null) {
            ventanaErrores.dispose();
            ventanaErrores = null;
        }

        // Limpiar áreas de texto
        areaCodigoFuente.setText("");
        areaArchivoFlex.setText("");
        areaArchivoCup.setText("");
        areaResultadoCompilacion.setText("");

        archivoFuenteActual = null;
        controlador.limpiarResultados();

        actualizarEstado("🔄 Limpieza completada - Listo para cargar nuevos archivos");
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void cargarArchivoEnArea(File archivo, JTextArea area) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            area.setText("");
            String linea;
            while ((linea = br.readLine()) != null) {
                area.append(linea + "\n");
            }
            area.setCaretPosition(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar archivo: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarEstado(String mensaje) {
        labelEstado.setText("  " + mensaje);
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(texto.length() > 20 ? 180 : 150, 32));

        // Efecto hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = btn.getBackground();

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(originalColor.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(originalColor);
            }
        });

        return btn;
    }

    private JTextArea crearTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        area.setBackground(BG_DARKER);
        area.setForeground(FG_LIGHT);
        area.setCaretColor(FG_LIGHT);
        area.setLineWrap(false);
        area.setTabSize(4);
        area.setSelectionColor(new Color(0x214283));
        area.setSelectedTextColor(FG_LIGHT);
        return area;
    }

    private JTable crearTablaEstilizada(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setBackground(BG_DARKER);
        tabla.setForeground(FG_LIGHT);
        tabla.setGridColor(BORDER_DARK);
        tabla.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        tabla.setSelectionBackground(new Color(0x214283));
        tabla.setSelectionForeground(FG_LIGHT);
        tabla.getTableHeader().setBackground(BG_DARK);
        tabla.getTableHeader().setForeground(FG_LIGHT);
        tabla.getTableHeader().setFont(new Font("JetBrains Mono", Font.BOLD, 12));
        tabla.setRowHeight(28);
        tabla.setShowGrid(true);
        tabla.setIntercellSpacing(new Dimension(1, 1));
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return tabla;
    }

    private JPanel crearPanelEditor(String titulo, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_DARK),
                titulo);
        border.setTitleColor(FG_LIGHT);
        border.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 12));
        panel.setBorder(border);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBackground(BG_DARKER);
        scroll.getViewport().setBackground(BG_DARKER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelConScroll(String titulo, JComponent componente) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_DARK),
                titulo);
        border.setTitleColor(FG_LIGHT);
        border.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 12));
        panel.setBorder(border);

        if (!(componente instanceof JScrollPane)) {
            JScrollPane scroll = new JScrollPane(componente);
            scroll.setBackground(BG_DARKER);
            scroll.getViewport().setBackground(BG_DARKER);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            panel.add(scroll, BorderLayout.CENTER);
        } else {
            panel.add(componente, BorderLayout.CENTER);
        }

        return panel;
    }
}