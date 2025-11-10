package com.presentacion.AnalizadorProyectoFinal;

import com.persistencia.analizadorSintacticoLR.tablaLR.lr0Table;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

public class VentanaTablaLR extends JFrame {
    
    public VentanaTablaLR(lr0Table.Result tabla) {
        setTitle("2. Tabla de Análisis Sintáctico LR(0)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior con título
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        JLabel titulo = new JLabel("TABLA DE ANÁLISIS SINTÁCTICO LR(0)");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(titulo);
        add(topPanel, BorderLayout.NORTH);
        
        // Construir encabezados de la tabla
        java.util.List<String> headers = new ArrayList<>();
        headers.add("Estado");
        
        // Columnas de ACCIÓN
        for (String t : tabla.terminals) {
            headers.add(t);
        }
        
        // Columnas de IR_A
        for (String nt : tabla.nonTerminals) {
            headers.add(nt);
        }
        
        // Construir datos de la tabla
        Object[][] data = new Object[tabla.states][headers.size()];
        
        for (int i = 0; i < tabla.states; i++) {
            data[i][0] = "I" + i;
            
            // Llenar ACCIÓN
            int col = 1;
            for (String t : tabla.terminals) {
                String accion = tabla.action.getOrDefault(i, Collections.emptyMap()).get(t);
                data[i][col++] = (accion != null) ? accion : "";
            }
            
            // Llenar IR_A
            for (String nt : tabla.nonTerminals) {
                Integer estado = tabla.gotoTable.getOrDefault(i, Collections.emptyMap()).get(nt);
                data[i][col++] = (estado != null) ? estado.toString() : "";
            }
        }
        
        DefaultTableModel model = new DefaultTableModel(data, headers.toArray()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tablaLR = new JTable(model);
        tablaLR.setFont(new Font("Consolas", Font.PLAIN, 11));
        tablaLR.setRowHeight(22);
        tablaLR.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Ajustar anchos de columnas
        tablaLR.getColumnModel().getColumn(0).setPreferredWidth(70); // Estado
        for (int i = 1; i < headers.size(); i++) {
            tablaLR.getColumnModel().getColumn(i).setPreferredWidth(60);
        }
        
        JScrollPane scroll = new JScrollPane(tablaLR);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        add(scroll, BorderLayout.CENTER);
        
        // Panel inferior con información
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        JLabel info = new JLabel(String.format(
            "Estados: %d | Terminales: %d | No Terminales: %d", 
            tabla.states, tabla.terminals.size(), tabla.nonTerminals.size()
        ));
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottomPanel.add(info);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}