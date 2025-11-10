package com.presentacion.AnalizadorProyectoFinal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VentanaTokensLexicos extends JFrame {
    
    public VentanaTokensLexicos(Object[][] tokens, Object[][] simbolos) {
        setTitle("1. Análisis Léxico - Tira de Tokens");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior con título
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        JLabel titulo = new JLabel("ANÁLISIS LÉXICO - Tira de Tokens");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(titulo);
        add(topPanel, BorderLayout.NORTH);
        
        // Panel con pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // PESTAÑA 1: Tokens
        String[] columnasTokens = {"Línea", "Lexema", "Token"};
        DefaultTableModel modelTokens = new DefaultTableModel(tokens, columnasTokens) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tablaTokens = new JTable(modelTokens);
        tablaTokens.setFont(new Font("Consolas", Font.PLAIN, 12));
        tablaTokens.setRowHeight(22);
        tablaTokens.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaTokens.getColumnModel().getColumn(1).setPreferredWidth(200);
        tablaTokens.getColumnModel().getColumn(2).setPreferredWidth(200);
        
        JScrollPane scrollTokens = new JScrollPane(tablaTokens);
        tabbedPane.addTab("Tokens (" + tokens.length + ")", scrollTokens);
        
        // PESTAÑA 2: Tabla de Símbolos
        String[] columnasSimbolos = {"ID", "Identificador"};
        DefaultTableModel modelSimbolos = new DefaultTableModel(simbolos, columnasSimbolos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tablaSimbolos = new JTable(modelSimbolos);
        tablaSimbolos.setFont(new Font("Consolas", Font.PLAIN, 12));
        tablaSimbolos.setRowHeight(22);
        tablaSimbolos.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaSimbolos.getColumnModel().getColumn(1).setPreferredWidth(300);
        
        JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
        tabbedPane.addTab("Tabla de Símbolos (" + simbolos.length + ")", scrollSimbolos);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Panel inferior con información
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        JLabel info = new JLabel("Total de tokens: " + tokens.length + " | Identificadores únicos: " + simbolos.length);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomPanel.add(info);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}