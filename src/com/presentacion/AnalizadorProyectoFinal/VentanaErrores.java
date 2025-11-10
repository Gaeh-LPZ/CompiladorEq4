package com.presentacion.AnalizadorProyectoFinal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VentanaErrores extends JFrame {
    
    public VentanaErrores(Object[][] errores) {
        setTitle("4. Tabla de Errores");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior con título
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        JLabel titulo = new JLabel("TABLA DE ERRORES");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(titulo);
        add(topPanel, BorderLayout.NORTH);
        
        if (errores.length == 0) {
            // No hay errores
            JPanel centerPanel = new JPanel(new GridBagLayout());
            JLabel noErrores = new JLabel("No se encontraron errores léxicos ni sintácticos");
            noErrores.setFont(new Font("Segoe UI", Font.BOLD, 16));
            noErrores.setForeground(new Color(0, 128, 0));
            centerPanel.add(noErrores);
            add(centerPanel, BorderLayout.CENTER);
        } else {
            // Hay errores - mostrar tabla
            String[] columnas = {"Línea/Paso", "Tipo", "Descripción"};
            DefaultTableModel model = new DefaultTableModel(errores, columnas) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            JTable tablaErrores = new JTable(model);
            tablaErrores.setFont(new Font("Consolas", Font.PLAIN, 12));
            tablaErrores.setRowHeight(24);
            
            // Ajustar anchos de columnas
            tablaErrores.getColumnModel().getColumn(0).setPreferredWidth(100); // Línea/Paso
            tablaErrores.getColumnModel().getColumn(1).setPreferredWidth(120); // Tipo
            tablaErrores.getColumnModel().getColumn(2).setPreferredWidth(600); // Descripción
            
            JScrollPane scroll = new JScrollPane(tablaErrores);
            add(scroll, BorderLayout.CENTER);
        }
        
        // Panel inferior con información
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        int erroresLexicos = 0;
        int erroresSintacticos = 0;
        
        for (Object[] error : errores) {
            String tipo = (String) error[1];
            if (tipo.equals("Léxico")) {
                erroresLexicos++;
            } else if (tipo.equals("Sintáctico")) {
                erroresSintacticos++;
            }
        }
        
        String mensaje = String.format(
            "Total de errores: %d | Léxicos: %d | Sintácticos: %d",
            errores.length, erroresLexicos, erroresSintacticos
        );
        
        JLabel info = new JLabel(mensaje);
        info.setFont(new Font("Segoe UI", Font.BOLD, 12));
        info.setForeground(errores.length > 0 ? Color.RED : new Color(0, 128, 0));
        bottomPanel.add(info);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}