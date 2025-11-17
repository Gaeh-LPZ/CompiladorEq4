package com.presentacion.analizadorSemantico;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;


import com.persistencia.analizadorSemantico.AccionSemantica;
import com.persistencia.analizadorSemantico.PasoAnalisis;
import com.persistencia.analizadorSemantico.ResultadoAnalisis;
import com.persistencia.analizadorSemantico.AnalizadorSemanticoLR;

public class analizadorSemanticoPanel extends JPanel {

    private static final Color BG_DARK = new Color(0x1E1F22);
    private static final Color FG_LIGHT = new Color(0xE6E9EE);
    private static final Color BORDER_DARK = new Color(0x2A2D31);
    private static final Color BUTTON_BG = new Color(0x3574F0);
    
    // Componentes para cargar archivos
    private JTextField txtGramatica;
    private JTextField txtAcciones;
    private JTextField txtCodigo;
    private JButton btnCargarGramatica;
    private JButton btnCargarAcciones;
    private JButton btnCargarCodigo;
    private JButton btnEjecutar;
    private JButton btnLimpiar;
    
    // Pestañas para mostrar resultados
    private JTabbedPane tabbedResultados;
    private JTextArea areaGramatica;
    private JTextArea areaAcciones;
    private JTextArea areaCodigo;
    private JTextArea areaTiraTokens;
    private JTable tablaCorrida;
    private DefaultTableModel modeloCorrida;
    private JTextArea areaResultado;
    
    public analizadorSemanticoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        initComponents();
    }
    
    private void initComponents() {
        // Panel superior: Carga de archivos
        add(createPanelCarga(), BorderLayout.NORTH);
        
        // Panel central: Pestañas con resultados
        add(createPanelResultados(), BorderLayout.CENTER);
    }
    
    private JPanel createPanelCarga() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_DARK),
            "Carga de Archivos",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("JetBrains Mono", Font.BOLD, 12),
            FG_LIGHT
        ));
        
        // Fila 1: Gramática
        JPanel filaGramatica = createFilaCarga(
            "Gramática:",
            txtGramatica = new JTextField(),
            btnCargarGramatica = new JButton("📁 Cargar")
        );
        
        // Fila 2: Acciones Semánticas
        JPanel filaAcciones = createFilaCarga(
            "Acciones Semánticas:",
            txtAcciones = new JTextField(),
            btnCargarAcciones = new JButton("📁 Cargar")
        );
        
        // Fila 3: Código Fuente
        JPanel filaCodigo = createFilaCarga(
            "Código Fuente:",
            txtCodigo = new JTextField(),
            btnCargarCodigo = new JButton("📁 Cargar")
        );
        
        panel.add(filaGramatica);
        panel.add(Box.createVerticalStrut(8));
        panel.add(filaAcciones);
        panel.add(Box.createVerticalStrut(8));
        panel.add(filaCodigo);
        panel.add(Box.createVerticalStrut(12));
        
        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelBotones.setBackground(BG_DARK);
        
        btnEjecutar = createStyledButton("▶ Ejecutar Análisis", BUTTON_BG);
        btnLimpiar = createStyledButton("🗑 Limpiar", new Color(0x6C707E));
        
        panelBotones.add(btnEjecutar);
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
            new EmptyBorder(5, 8, 5, 8)
        ));
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
        btn.setPreferredSize(new Dimension(180, 35));
        
        // Efecto hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    private JPanel createPanelResultados() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        
        tabbedResultados = new JTabbedPane();
        tabbedResultados.setBackground(BG_DARK);
        tabbedResultados.setForeground(FG_LIGHT);
        
        // Pestaña 1: Gramática
        areaGramatica = createTextArea();
        tabbedResultados.addTab("📝 Gramática", createScrollPane(areaGramatica));
        
        // Pestaña 2: Acciones Semánticas
        areaAcciones = createTextArea();
        tabbedResultados.addTab("⚙️ Acciones Semánticas", createScrollPane(areaAcciones));
        
        // Pestaña 3: Código Fuente
        areaCodigo = createTextArea();
        tabbedResultados.addTab("📄 Código Fuente", createScrollPane(areaCodigo));
        
        // Pestaña 4: Tira de Tokens
        areaTiraTokens = createTextArea();
        tabbedResultados.addTab("🔤 Tira de Tokens", createScrollPane(areaTiraTokens));
        
        // Pestaña 5: Corrida del Análisis (Tabla)
        tablaCorrida = createTableCorrida();
        tabbedResultados.addTab("▶️ Corrida del Análisis", createScrollPane(tablaCorrida));
        
        // Pestaña 6: Resultado/Traducción
        areaResultado = createTextArea();
        tabbedResultados.addTab("✅ Resultado/Traducción", createScrollPane(areaResultado));
        
        panel.add(tabbedResultados, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        area.setBackground(new Color(0x2B2D30));
        area.setForeground(FG_LIGHT);
        area.setCaretColor(FG_LIGHT);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        area.setLineWrap(false);
        area.setWrapStyleWord(false);
        return area;
    }
    
    private JTable createTableCorrida() {
        String[] columnas = {"Paso", "Pila", "Entrada", "Acción", "Salida"};
        modeloCorrida = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tabla = new JTable(modeloCorrida);
        tabla.setBackground(new Color(0x2B2D30));
        tabla.setForeground(FG_LIGHT);
        tabla.setGridColor(BORDER_DARK);
        tabla.setSelectionBackground(new Color(0x3574F0));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        tabla.getTableHeader().setBackground(new Color(0x3C3F41));
        tabla.getTableHeader().setForeground(FG_LIGHT);
        tabla.getTableHeader().setFont(new Font("JetBrains Mono", Font.BOLD, 11));
        tabla.setRowHeight(25);
        
        // Ajustar anchos de columnas
        tabla.getColumnModel().getColumn(0).setPreferredWidth(60);  // Paso
        tabla.getColumnModel().getColumn(1).setPreferredWidth(200); // Pila
        tabla.getColumnModel().getColumn(2).setPreferredWidth(200); // Entrada
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150); // Acción
        tabla.getColumnModel().getColumn(4).setPreferredWidth(250); // Salida
        
        return tabla;
    }
    
    private JScrollPane createScrollPane(Component component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(new Color(0x2B2D30));
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_DARK));
        return scroll;
    }
    
    private void setupEventListeners() {
        // Cargar Gramática
        btnCargarGramatica.addActionListener(e -> {
            File archivo = seleccionarArchivo("Seleccionar archivo de gramática");
            if (archivo != null) {
                txtGramatica.setText(archivo.getAbsolutePath());
                cargarGramatica(archivo);
            }
        });
        
        // Cargar Acciones
        btnCargarAcciones.addActionListener(e -> {
            File archivo = seleccionarArchivo("Seleccionar archivo de acciones semánticas");
            if (archivo != null) {
                txtAcciones.setText(archivo.getAbsolutePath());
                cargarAcciones(archivo);
            }
        });
        
        // Cargar Código
        btnCargarCodigo.addActionListener(e -> {
            File archivo = seleccionarArchivo("Seleccionar archivo de código fuente");
            if (archivo != null) {
                txtCodigo.setText(archivo.getAbsolutePath());
                cargarCodigo(archivo);
            }
        });
        
        // Ejecutar
        btnEjecutar.addActionListener(e -> ejecutarAnalisis());
        
        // Limpiar
        btnLimpiar.addActionListener(e -> limpiarTodo());
    }
    
    private File seleccionarArchivo(String titulo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(titulo);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Establecer carpeta inicial en /pruebas
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
    
    private void cargarGramatica(File archivo) {
        try {
            String contenido = new String(Files.readAllBytes(archivo.toPath()));
            areaGramatica.setText(contenido);
            tabbedResultados.setSelectedIndex(0);
        } catch (Exception ex) {
            mostrarError("Error al cargar gramática: " + ex.getMessage());
        }
    }
    
    private void cargarAcciones(File archivo) {
        try {
            String contenido = new String(Files.readAllBytes(archivo.toPath()));
            areaAcciones.setText(contenido);
            tabbedResultados.setSelectedIndex(1);
        } catch (Exception ex) {
            mostrarError("Error al cargar acciones: " + ex.getMessage());
        }
    }
    
    private void cargarCodigo(File archivo) {
        try {
            String contenido = new String(Files.readAllBytes(archivo.toPath()));
            areaCodigo.setText(contenido);
            tabbedResultados.setSelectedIndex(2);
        } catch (Exception ex) {
            mostrarError("Error al cargar código: " + ex.getMessage());
        }
    }
    
    private void ejecutarAnalisis() {
    if (txtGramatica.getText().isEmpty() || 
        txtAcciones.getText().isEmpty() || 
        txtCodigo.getText().isEmpty()) {
        mostrarError("Debe cargar todos los archivos antes de ejecutar el análisis");
        return;
    }
    
    try {
        // Crear analizador semántico
        AnalizadorSemanticoLR analizador = new AnalizadorSemanticoLR();
        
        // Cargar acciones semánticas desde el archivo
        String rutaAcciones = txtAcciones.getText();
        analizador.cargarAcciones(rutaAcciones);
        
        // Ejecutar análisis completo
        String rutaGramatica = txtGramatica.getText();
        String rutaCodigo = txtCodigo.getText();
        ResultadoAnalisis resultado = analizador.analizar(rutaGramatica, rutaCodigo);  
        
        // Mostrar tokens en pestaña 4
        StringBuilder sbTokens = new StringBuilder();
        for (String token : resultado.getTokens()) {
            sbTokens.append(token).append(" ");
        }
        areaTiraTokens.setText(sbTokens.toString().trim());
        
        // Mostrar corrida en pestaña 5 (tabla)
        modeloCorrida.setRowCount(0);
        for (PasoAnalisis paso : resultado.getCorrida()) {  
            modeloCorrida.addRow(paso.toArray());
        }
        
        // Mostrar resultado/traducción en pestaña 6
        if (resultado.getResultado() != null) {
            areaResultado.setText("✅ Análisis exitoso\n\n");
            areaResultado.append("Resultado: " + resultado.getResultado());
        } else {
            areaResultado.setText("✅ Análisis completado sin errores");
        }
        
        // Cambiar a la pestaña de corrida
        tabbedResultados.setSelectedIndex(4);
        
        JOptionPane.showMessageDialog(this, 
            "Análisis completado exitosamente", 
            "Éxito", 
            JOptionPane.INFORMATION_MESSAGE);
        
    } catch (Exception ex) {
        mostrarError("Error en el análisis:\n" + ex.getMessage());
        ex.printStackTrace();
        
        // Mostrar error detallado en resultado
        areaResultado.setText("❌ ERROR:\n\n" + ex.getMessage());
        tabbedResultados.setSelectedIndex(5);
    }
}
    
    private void limpiarTodo() {
        txtGramatica.setText("");
        txtAcciones.setText("");
        txtCodigo.setText("");
        areaGramatica.setText("");
        areaAcciones.setText("");
        areaCodigo.setText("");
        areaTiraTokens.setText("");
        modeloCorrida.setRowCount(0);
        areaResultado.setText("");
        tabbedResultados.setSelectedIndex(0);
    }
    
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, 
            mensaje, 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}