package com.presentacion;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.presentacion.analizadorLexico.AnalizadorLexicoPanel;
import com.presentacion.analizadorSintacticoLR.AnalizadorLRPanel;
import com.presentacion.coleccionCanonica.ColeccionCanonicaPanel;
import com.presentacion.primerosSiguientes.PrimerosSiguientesPanel;
import com.presentacion.tablaAnalisisSintacticoLR.TablaLRPanel;
import com.presentacion.AnalizadorProyectoFinal.AnalizadorProyectoFinalPanel;
import com.presentacion.analizadorSemantico.analizadorSemanticoPanel;
import com.presentacion.analizadorSemanticoFinal.analizadorSemanticoFinalPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VentanaPrincipal extends JFrame {

    private static final Color BG_DARK = new Color(0x1E1F22);
    private static final Color FG_LIGHT = new Color(0xE6E9EE);

    private DataProvider dataProvider;
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;

    public VentanaPrincipal(DataProvider provider) {
        super("Compilador");
        this.dataProvider = provider;
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 640);
        setLocationRelativeTo(null);

        setJMenuBar(new ReusableMenuBar(dataProvider, new MenuActionHandler()));

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_DARK);
        tabbedPane.setForeground(FG_LIGHT);
        tabbedPane.addTab("Inicio", createBannerPanel());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        statusLabel = new JLabel("Listo");
        statusLabel.setBorder(new EmptyBorder(6, 8, 6, 8));
        statusLabel.setForeground(FG_LIGHT);
        statusLabel.setBackground(BG_DARK);
        statusLabel.setOpaque(true);
        getContentPane().add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createBannerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridy = 0;

        CodeSnippetPanel codePanel = new CodeSnippetPanel(
                "public class HolaMundo {\r\n"
                        + "    public static void main(String[] args) {\r\n"
                        + "        System.out.println(\"hola mundo\");\r\n"
                        + "    }\r\n"
                        + "}");
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(codePanel, gbc);

        ImagePanel imagePanel = new ImagePanel(dataProvider.getBannerImagePath());
        imagePanel.setBackground(BG_DARK);
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(imagePanel, gbc);

        return panel;
    }

    private void openAnalyzerTab(String title, JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBackground(BG_DARK);

        tabbedPane.addTab(title, scroll);
        int index = tabbedPane.indexOfComponent(scroll);

        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabHeader.setOpaque(false);

        JLabel tabLabel = new JLabel(title + "  ");
        tabLabel.setForeground(FG_LIGHT);

        JButton closeBtn = new JButton("✕");
        closeBtn.setMargin(new Insets(0, 4, 0, 4));
        closeBtn.setBorder(BorderFactory.createEmptyBorder());
        closeBtn.setFocusable(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setForeground(FG_LIGHT);

        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeBtn.setOpaque(true);
                closeBtn.setBackground(new Color(220, 70, 70));
                closeBtn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeBtn.setOpaque(false);
                closeBtn.setBackground(null);
                closeBtn.setForeground(FG_LIGHT);
            }
        });

        closeBtn.addActionListener(k -> tabbedPane.remove(scroll));

        tabHeader.add(tabLabel);
        tabHeader.add(closeBtn);

        tabbedPane.setTabComponentAt(index, tabHeader);
        tabbedPane.setSelectedComponent(scroll);

        statusLabel.setText("Abierta: " + title);
    }

    private class MenuActionHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            switch (cmd) {
                case "exit":
                    dispose();
                    break;
                case "analizador_lexico":
                    openAnalyzerTab("Analizador Léxico", new AnalizadorLexicoPanel());
                    break;

                case "primeros_siguientes":
                    openAnalyzerTab("Algoritmo primeros y siguientes", new PrimerosSiguientesPanel());
                    break;

                case "coleccion_canonica":
                    openAnalyzerTab("Algoritmo Colección Canónica", new ColeccionCanonicaPanel());
                    break;
                case "tabla_lr0":
                    openAnalyzerTab("Tabla LR(0)", new TablaLRPanel());
                    break;
                case "analizador_sintactico":
                    openAnalyzerTab("Analizador Sintáctico LR", new AnalizadorLRPanel());
                    break;
                case "analizador_sintactico_proyecto":
                    openAnalyzerTab("Analizador Sintáctico LR - Proyecto Final", new AnalizadorProyectoFinalPanel());
                    break;
                case "analizador_semantico":
                    openAnalyzerTab("Analizador Semántico LR", new analizadorSemanticoPanel());
                    break;
                case "analizador_semantico_final":
                    openAnalyzerTab("Analizador Semántico Final", new analizadorSemanticoFinalPanel());
                    break;

                default:
                    String content = dataProvider.getAlgorithmDescription(cmd);
                    if (content == null) {
                        content = "(sin contenido)";
                    }
                    JTextArea area = new JTextArea(content);
                    area.setEditable(false);
                    area.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
                    area.setForeground(FG_LIGHT);
                    area.setBackground(BG_DARK);
                    area.setCaretPosition(0);

                    JScrollPane sp = new JScrollPane(area);
                    sp.getViewport().setBackground(BG_DARK);
                    sp.setBackground(BG_DARK);

                    openAnalyzerTab(cmd, sp);
                    break;
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            UIManager.put("defaultFont", new Font("JetBrains Mono", Font.PLAIN, 13));

            UIManager.put("Panel.background", BG_DARK);
            UIManager.put("TabbedPane.background", BG_DARK);
            UIManager.put("SplitPane.background", BG_DARK);
            UIManager.put("ScrollPane.background", BG_DARK);
            UIManager.put("Viewport.background", BG_DARK);
            UIManager.put("TextArea.background", BG_DARK);
            UIManager.put("TextField.background", BG_DARK);
            UIManager.put("Table.background", BG_DARK);
            UIManager.put("Table.alternateRowColor", BG_DARK);

            UIManager.put("Label.foreground", FG_LIGHT);
            UIManager.put("Menu.foreground", FG_LIGHT);
            UIManager.put("MenuItem.foreground", FG_LIGHT);
            UIManager.put("Button.foreground", FG_LIGHT);
            UIManager.put("TabbedPane.foreground", FG_LIGHT);
            UIManager.put("Table.foreground", FG_LIGHT);
            UIManager.put("TextArea.foreground", FG_LIGHT);
            UIManager.put("TextField.foreground", FG_LIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            DataProvider provider = new DefaultDataProvider();
            VentanaPrincipal frame = new VentanaPrincipal(provider);
            frame.setVisible(true);
        });
    }
}

class ReusableMenuBar extends JMenuBar {

    public ReusableMenuBar(DataProvider provider, ActionListener handler) {
        setBackground(new Color(0x1E1F22));
        setForeground(new Color(0xE6E9EE));

        JMenu archivo = new JMenu("Salir");
        JMenuItem exit = new JMenuItem("Salir");
        exit.setActionCommand("exit");
        exit.addActionListener(handler);
        archivo.add(exit);
        add(archivo);

        JMenu lex = new JMenu("Analizador léxico");
        JMenuItem thompson = new JMenuItem("Algoritmo de Thompson AFN");
        thompson.setActionCommand("thompson_afn");
        thompson.addActionListener(handler);
        lex.add(thompson);

        JMenuItem conjuntos = new JMenuItem("Algoritmo de Construcción de Conjuntos");
        conjuntos.setActionCommand("construccion_conjuntos");
        conjuntos.addActionListener(handler);
        lex.add(conjuntos);

        JMenuItem analizadorLex = new JMenuItem("Analizador léxico");
        analizadorLex.setActionCommand("analizador_lexico");
        analizadorLex.addActionListener(handler);
        lex.add(analizadorLex);
        add(lex);

        JMenu sint = new JMenu("Analizador sintáctico");

        JMenuItem firstFollow = new JMenuItem("Algoritmo primeros y siguientes");
        firstFollow.setActionCommand("primeros_siguientes");
        firstFollow.addActionListener(handler);
        sint.add(firstFollow);

        JMenuItem coleccionCanonica = new JMenuItem("Algoritmo Colección Canónica");
        coleccionCanonica.setActionCommand("coleccion_canonica");
        coleccionCanonica.addActionListener(handler);
        sint.add(coleccionCanonica);

        JMenuItem tablaLR0 = new JMenuItem("Tabla de Análisis Sintáctico LR(0)");
        tablaLR0.setActionCommand("tabla_lr0");
        tablaLR0.addActionListener(handler);
        sint.add(tablaLR0);

        JMenuItem parser = new JMenuItem("Analizador sintáctico");
        parser.setActionCommand("analizador_sintactico");
        parser.addActionListener(handler);
        sint.add(parser);

        sint.addSeparator();
        JMenuItem parserProyecto = new JMenuItem("Analizador Sintáctico LR - Proyecto Final");
        parserProyecto.setActionCommand("analizador_sintactico_proyecto");
        parserProyecto.addActionListener(handler);
        sint.add(parserProyecto);
        add(sint);

        JMenu sem = new JMenu("Analizador semántico");
        JMenuItem semItem = new JMenuItem("Analizador semántico (placeholder)");
        JMenuItem semItemFinal = new JMenuItem("Analizador semántico Final");
        semItem.setActionCommand("analizador_semantico");
        semItemFinal.setActionCommand("analizador_semantico_final");
        semItemFinal.addActionListener(handler);
        semItem.addActionListener(handler);
        sem.add(semItem);
        sem.add(semItemFinal);
        add(sem);
    }
}

class ImagePanel extends JPanel {

    private BufferedImage image;

    public ImagePanel(String basePath) {
        setOpaque(true);
        setBackground(new Color(0x1E1F22));
        try {
            File file = findImageFile(basePath);
            if (file == null) {
                throw new IOException("No se encontró ninguna imagen con ese nombre");
            }
            image = ImageIO.read(file);
        } catch (IOException e) {
            image = null;
            System.err.println("No se pudo cargar la imagen: " + e.getMessage());
        }
    }

    private File findImageFile(String basePath) {
        File base = new File(basePath);
        File dir = base.getParentFile();
        if (dir == null) {
            return null;
        }

        final String prefix = base.getName();
        String[] validExts = ImageIO.getReaderFileSuffixes();

        File[] matches = dir.listFiles(f -> {
            if (!f.isFile()) {
                return false;
            }
            String name = f.getName().toLowerCase();
            if (!name.startsWith(prefix.toLowerCase())) {
                return false;
            }
            for (String ext : validExts) {
                if (name.endsWith("." + ext.toLowerCase())) {
                    return true;
                }
            }
            return false;
        });

        return (matches != null && matches.length > 0) ? matches[0] : null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            int w = getWidth(), h = getHeight();
            double imgRatio = (double) image.getWidth() / image.getHeight();
            double panelRatio = (double) w / h;
            int drawW, drawH;
            if (panelRatio > imgRatio) {
                drawW = w;
                drawH = (int) (w / imgRatio);
            } else {
                drawH = h;
                drawW = (int) (h * imgRatio);
            }
            int x = (w - drawW) / 2;
            int y = (h - drawH) / 2;
            g.drawImage(image, x, y, drawW, drawH, this);
        } else {
            g.setColor(new Color(0x9AA3B2));
            g.setFont(g.getFont().deriveFont(Font.ITALIC, 14f));
            drawCenteredString(g,
                    "Sin imagen: coloca resources/compilador_banner.(png/jpg/…)",
                    getWidth(), getHeight());
        }
    }

    private void drawCenteredString(Graphics g, String text, int w, int h) {
        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(text)) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
    }
}

class CodeSnippetPanel extends JPanel {

    private static final Color BG_DARK = new Color(0x1E1F22);
    private static final Color FG_LIGHT = new Color(0xE6E9EE);
    private static final Color BORDER_DARK = new Color(0x2A2D31);

    public CodeSnippetPanel(String code) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_DARK),
                new EmptyBorder(12, 12, 12, 12)));
        setBackground(BG_DARK);

        JTextArea area = new JTextArea(code);
        area.setEditable(false);
        area.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        area.setForeground(FG_LIGHT);
        area.setBackground(BG_DARK);
        area.setCaretPosition(0);
        add(area, BorderLayout.CENTER);

        setPreferredSize(new Dimension(420, 220));
    }
}

interface DataProvider {

    String getBannerImagePath();

    String getAlgorithmDescription(String key);
}

class DefaultDataProvider implements DataProvider {

    @Override
    public String getBannerImagePath() {
        return "src/com/presentacion/resources/compilador_banner";
    }

    @Override
    public String getAlgorithmDescription(String key) {
        switch (key) {
            case "thompson_afn":
                return "Algoritmo de Thompson (AFN):\n\nConstruye un AFN a partir de una ER.";
            case "construccion_conjuntos":
                return "Algoritmo de Construcción de Conjuntos:\n\nConvierte AFN a AFD usando e-closure y move.";
            case "analizador_lexico":
                return "Analizador léxico (placeholder):\n\nEjecuta análisis léxico sobre texto de entrada.";
            case "analizador_semantico":
                return "Analizador semántico (placeholder):\n\nChequeos semánticos y reporte de errores.";
            case "analizador_semantico_final":
                return "Analizador semántico Final:\n\nChequeos semánticos y reporte de errores.";

            default:
                return "Comando: " + key + "\n\n(No hay contenido preparado para este comando.)";
        }
    }
}
