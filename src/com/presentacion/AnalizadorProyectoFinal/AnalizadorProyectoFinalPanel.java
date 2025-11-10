package com.presentacion.AnalizadorProyectoFinal;

import com.persistencia.analizadorLexico.lexer;
import com.persistencia.analizadorLexico.token;
import com.persistencia.analizadorLexico.tipoToken;
import com.persistencia.analizadorSintacticoLR.tablaLR.lr0Table;
import com.persistencia.analizadorSintacticoLR.tablaLR.lrParser;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class AnalizadorProyectoFinalPanel extends JPanel {

    private final JTextField txtProgPath = new JTextField();
    private final JButton btnOpenP = new JButton("Abrir programa");
    private final JButton btnAnalyze = new JButton("Analizar");
    private final JButton btnClear = new JButton("Limpiar");

    private final JTextArea txtProgram = new JTextArea();

    private File programFile = null;
    
    // Ruta fija de la gramática del proyecto final
    private static final String GRAMMAR_PATH = "pruebas/gramaticas aumentadas/gramaticaFinal.txt";

    public AnalizadorProyectoFinalPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Panel superior
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Título informativo
        c.weightx = 0; 
        c.gridx = 0; 
        c.gridy = 0; 
        c.gridwidth = 3;
        JLabel lblInfo = new JLabel("Analizador Léxico y Sintáctico LR - Proyecto Final");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        top.add(lblInfo, c);

        c.gridy = 1;
        c.gridwidth = 1;
        top.add(new JLabel("Seleccione un programa fuente (o escriba abajo):"), c);
        
        c.weightx = 1; 
        c.gridx = 1; 
        txtProgPath.setEditable(false); 
        top.add(txtProgPath, c);
        
        c.weightx = 0; 
        c.gridx = 2; 
        top.add(btnOpenP, c);

        // Panel de botones de acción
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        btnAnalyze.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        btnPanel.add(btnAnalyze);
        btnPanel.add(btnClear);
        
        top.add(btnPanel, c);

        add(top, BorderLayout.NORTH);

        // Área de texto para el programa
        txtProgram.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        txtProgram.setLineWrap(false);
        txtProgram.setTabSize(4);

        JPanel center = new JPanel(new BorderLayout());
        JLabel lblProgram = new JLabel("Programa fuente (editable):");
        lblProgram.setFont(new Font("Segoe UI", Font.BOLD, 12));
        center.add(lblProgram, BorderLayout.NORTH);
        center.add(new JScrollPane(txtProgram), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // Listeners
        btnOpenP.addActionListener(this::onOpenProgram);
        btnAnalyze.addActionListener(this::onAnalyze);
        btnClear.addActionListener(e -> {
            programFile = null;
            txtProgPath.setText("");
            txtProgram.setText("");
        });
    }

    private void onOpenProgram(ActionEvent e) {
        JFileChooser ch = new JFileChooser(new File(System.getProperty("user.dir")));
        ch.setAcceptAllFileFilterUsed(true);
        ch.addChoosableFileFilter(new FileNameExtensionFilter("Java (*.java)", "java"));
        ch.addChoosableFileFilter(new FileNameExtensionFilter("Texto (*.txt)", "txt"));
        ch.addChoosableFileFilter(new FileNameExtensionFilter("Código (*.c, *.java, *.txt)", "c","java","txt"));
        
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            programFile = ch.getSelectedFile();
            txtProgPath.setText(programFile.getAbsolutePath());
            try {
                txtProgram.setText(Files.readString(programFile.toPath()));
                txtProgram.setCaretPosition(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo leer el programa:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAnalyze(ActionEvent e) {
        try {
            // 1. Verificar que existe la gramática del proyecto
            File grammarFile = new File(GRAMMAR_PATH);
            if (!grammarFile.exists()) {
                JOptionPane.showMessageDialog(this, 
                    "No se encontró el archivo de gramática:\n" + GRAMMAR_PATH + 
                    "\n\nAsegúrate de que el archivo existe.",
                    "Error - Gramática no encontrada", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Obtener el programa a analizar
            String prog;
            if (programFile != null) {
                prog = Files.readString(programFile.toPath());
            } else {
                prog = txtProgram.getText();
                if (prog == null || prog.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Por favor, carga o escribe un programa fuente para analizar.",
                        "Atención",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // 3. ANÁLISIS LÉXICO usando tu Lexer
            lexer analizadorLexico = new lexer(prog);
            List<token> tokensLexicos = analizadorLexico.scanTokens();
            
            // Extraer errores léxicos
            List<Object[]> erroresLexicos = new ArrayList<>();
            for (token t : tokensLexicos) {
                if (t.tipo == tipoToken.DESCONOCIDO) {
                    erroresLexicos.add(new Object[]{ t.linea, "Léxico", "Token desconocido: '" + t.lexema + "'" });
                }
                if (t.tipo == tipoToken.ERROR_DE_CADENA) {
                    erroresLexicos.add(new Object[]{ t.linea, "Léxico", "Error de cadena: '" + t.lexema + "'" });
                }
            }

            // Preparar datos para la tabla de tokens
            List<Object[]> listaTokens = new ArrayList<>();
            for (token t : tokensLexicos) {
                if (t.tipo != tipoToken.EOF && t.tipo != tipoToken.DESCONOCIDO) {
                    Object tokenMostrar;
                    switch (t.tipo) {
                        case PUNTO:
                        case PUNTO_Y_COMA:
                        case COMA:
                        case DOS_PUNTOS:
                        case CORCHETE_IZQ:
                        case CORCHETE_DER:
                        case PARENTESIS_IZQ:
                        case PARENTESIS_DER:
                        case LLAVE_IZQ:
                        case LLAVE_DER:
                            tokenMostrar = t.lexema;
                            break;
                        default:
                            tokenMostrar = t.tipo;
                    }
                    listaTokens.add(new Object[]{t.linea, t.lexema, tokenMostrar});
                }
            }

            // Tabla de símbolos
            List<Object[]> listaSimbolos = new ArrayList<>();
            Set<String> idsUnicos = new HashSet<>();
            int idCounter = 1;
            for (token t : tokensLexicos) {
                if (t.tipo == tipoToken.IDENTIFICADOR && idsUnicos.add(t.lexema)) {
                    listaSimbolos.add(new Object[]{ idCounter++, t.lexema });
                }
            }

            // VENTANA 1: Mostrar tokens léxicos
            VentanaTokensLexicos ventanaTokens = new VentanaTokensLexicos(
                listaTokens.toArray(new Object[0][]),
                listaSimbolos.toArray(new Object[0][])
            );
            ventanaTokens.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            ventanaTokens.setVisible(true);

            // 4. Construir tabla LR desde la gramática fija
            lr0Table.Result tablaLR = lr0Table.buildFromFile(GRAMMAR_PATH);

            // VENTANA 2: Mostrar Tabla de Análisis Sintáctico LR
            VentanaTablaLR ventanaTablaLR = new VentanaTablaLR(tablaLR);
            ventanaTablaLR.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            ventanaTablaLR.setVisible(true);

            // 5. Convertir tokens del Lexer al formato esperado por LRParser
            List<String> tokensParaParser = convertirTokensParaParser(tokensLexicos);

            // 6. ANÁLISIS SINTÁCTICO - Obtener corrida paso a paso
            DefaultTableModel modeloCorrida = lrParser.runLRParse(GRAMMAR_PATH, tablaLR, tokensParaParser);

            // Extraer errores sintácticos del modelo
            List<Object[]> erroresSintacticos = new ArrayList<>();
            for (int i = 0; i < modeloCorrida.getRowCount(); i++) {
                String accion = (String) modeloCorrida.getValueAt(i, 2);
                if (accion != null && accion.startsWith("Error sintáctico")) {
                    String entrada = (String) modeloCorrida.getValueAt(i, 1);
                    erroresSintacticos.add(new Object[]{ i + 1, "Sintáctico", accion });
                }
            }

            // VENTANA 3: Mostrar corrida del análisis sintáctico
            VentanaCorridaSintactico ventanaCorrida = new VentanaCorridaSintactico(modeloCorrida);
            ventanaCorrida.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            ventanaCorrida.setVisible(true);

            // 7. Combinar errores léxicos y sintácticos
            List<Object[]> todosErrores = new ArrayList<>();
            todosErrores.addAll(erroresLexicos);
            todosErrores.addAll(erroresSintacticos);

            // VENTANA 4: Mostrar tabla de errores
            VentanaErrores ventanaErrores = new VentanaErrores(todosErrores.toArray(new Object[0][]));
            ventanaErrores.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            ventanaErrores.setVisible(true);

            // Mensaje de éxito
            String mensaje = "Análisis completado\n\n";
            mensaje += "Tokens generados: " + tokensLexicos.size() + "\n";
            mensaje += "Errores léxicos: " + erroresLexicos.size() + "\n";
            mensaje += "Errores sintácticos: " + erroresSintacticos.size() + "\n";
            mensaje += "\nSe abrieron 4 ventanas con los resultados.";
            JOptionPane.showMessageDialog(this, mensaje, "Análisis Exitoso", 
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al analizar:\n" + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private List<String> convertirTokensParaParser(List<token> tokens) {
        List<String> resultado = new ArrayList<>();
        
        for (token t : tokens) {
            String tokenStr = mapearTokenATerminal(t);
            if (tokenStr != null && !tokenStr.isEmpty()) {
                resultado.add(tokenStr);
            }
        }
        
        resultado.add("$");
        return resultado;
    }

    private String mapearTokenATerminal(token t) {
    switch (t.tipo) {
        // ============================================
        // PALABRAS RESERVADAS DE JAVA
        // ============================================
        case PACKAGE: return "PACKAGE";
        case IMPORT: return "IMPORT";
        case CLASS: return "CLASS";
        case INTERFACE: return "INTERFACE";
        case ENUM: return "ENUM";
        case PUBLIC: return "PUBLIC";
        case PRIVATE: return "PRIVATE";
        case PROTECTED: return "PROTECTED";
        case ABSTRACT: return "ABSTRACT";
        case STATIC: return "STATIC";
        case FINAL: return "FINAL";
        case SYNCHRONIZED: return "SYNCHRONIZED";
        case NATIVE: return "NATIVE";
        case TRANSIENT: return "TRANSIENT";
        case VOLATILE: return "VOLATILE";
        case EXTENDS: return "EXTENDS";
        case IMPLEMENTS: return "IMPLEMENTS";
        case VOID: return "VOID";
        case THROWS: return "THROWS";
        case CATCH: return "CATCH";
        case FINALLY: return "FINALLY";
        case TRUE: return "TRUE";
        case FALSE: return "FALSE";
        case NULL: return "NULL";
        case NEW: return "NEW";
        case THIS: return "THIS";
        case SUPER: return "SUPER";
        case INSTANCEOF: return "INSTANCEOF";
        case IF: return "IF";
        case ELSE: return "ELSE";
        case SWITCH: return "SWITCH";
        case CASE: return "CASE";
        case DEFAULT: return "DEFAULT";
        case WHILE: return "WHILE";
        case DO: return "DO";
        case FOR: return "FOR";
        case BREAK: return "BREAK";
        case CONTINUE: return "CONTINUE";
        case RETURN: return "RETURN";
        case THROW: return "THROW";
        case TRY: return "TRY";
        case ASSERT: return "ASSERT";
        
        // Tipos primitivos
        case BOOLEAN: return "BOOLEAN";
        case BYTE: return "BYTE";
        case SHORT: return "SHORT";
        case INT: return "INT";
        case LONG: return "LONG";
        case CHAR: return "CHAR";
        case FLOAT: return "FLOAT";
        case DOUBLE: return "DOUBLE";

        // ============================================
        // ⚠️ TOKENS ESPECIALES → MAPEAR A IDENTIFICADOR
        // ============================================
        // Estos NO son keywords de Java, son nombres de clases/métodos
        case MAIN:
        case CADENA:
        case SYSTEM:
        case OUT:
        case PRINTLN:
            return "IDENTIFICADOR";

        // ============================================
        // IDENTIFICADORES Y LITERALES
        // ============================================
        case IDENTIFICADOR: return "IDENTIFICADOR";
        case LITERAL_ENTERA: return "LITERAL_ENTERA";
        case LITERAL_FLOTANTE: return "LITERAL_FLOTANTE";
        case LITERAL_CADENA: return "LITERAL_CADENA";

        // ============================================
        // OPERADORES ARITMÉTICOS
        // ============================================
        case SUMA: return "SUMA";
        case RESTA: return "RESTA";
        case MULTIPLICACION: 
        case ASTERISK: return "ASTERISK";
        case DIVISION: return "DIVISION";
        case MOD: return "MOD";
        
        // ============================================
        // OPERADORES DE ASIGNACIÓN Y COMPARACIÓN
        // ============================================
        case ASIGNACION: return "ASIGNACION";
        case IGUAL: return "IGUAL";
        case DIFERENTE: return "DIFERENTE";
        case MENOR_QUE: return "MENOR_QUE";
        case MAYOR_QUE: return "MAYOR_QUE";
        case MENOR_IGUAL: return "MENOR_IGUAL";
        case MAYOR_IGUAL: return "MAYOR_IGUAL";
        
        // ============================================
        // OPERADORES LÓGICOS
        // ============================================
        case AND: return "AND";
        case OR: return "OR";
        case NOT: return "NOT";
        
        // ============================================
        // OPERADORES BIT A BIT
        // ============================================
        case BITAND: return "BITAND";
        case BITOR: return "BITOR";
        case BITXOR: return "BITXOR";
        case TILDE: return "TILDE";
        
        // ============================================
        // OPERADORES DE DESPLAZAMIENTO
        // ============================================
        case LSHIFT: return "LSHIFT";
        case RSHIFT: return "RSHIFT";
        case URSHIFT: return "URSHIFT";

        // ============================================
        // DELIMITADORES
        // ============================================
        case CORCHETE_IZQ: return "CORCHETE_IZQ";
        case CORCHETE_DER: return "CORCHETE_DER";
        case PARENTESIS_IZQ: return "PARENTESIS_IZQ";
        case PARENTESIS_DER: return "PARENTESIS_DER";
        case LLAVE_IZQ: return "LLAVE_IZQ";
        case LLAVE_DER: return "LLAVE_DER";
        case PUNTO_Y_COMA: return "PUNTO_Y_COMA";
        case COMA: return "COMA";
        case PUNTO: return "PUNTO";
        case DOS_PUNTOS:
        case COLON: return "COLON";
        case QUESTION: return "QUESTION";
        case MENOR: return "MENOR";
        case MAYOR: return "MAYOR";
        case ELLIPSIS: return "ELLIPSIS";
        case ARROBA: return "ARROBA";

        // ============================================
        // TOKENS ESPECIALES
        // ============================================
        case EOF: return "EOF";
        
        // ============================================
        // TOKENS A IGNORAR
        // ============================================
        case DESCONOCIDO:
        case ERROR_DE_CADENA:
        case COMILLA:
            return null;

        default:
            // Fallback: devolver el nombre del enum
            return t.tipo.name();
    }
}

}