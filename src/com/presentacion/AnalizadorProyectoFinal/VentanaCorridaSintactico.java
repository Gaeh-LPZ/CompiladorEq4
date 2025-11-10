package com.presentacion.AnalizadorProyectoFinal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VentanaCorridaSintactico extends JFrame {
    
    public VentanaCorridaSintactico(DefaultTableModel modeloCorrida) {
        setTitle("3. Corrida del Análisis Sintáctico");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior con título
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        JLabel titulo = new JLabel("CORRIDA DEL ANÁLISIS SINTÁCTICO");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(titulo);
        add(topPanel, BorderLayout.NORTH);
        
        // Descripción
        JPanel descPanel = new JPanel();
        descPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
        JLabel desc = new JLabel("Análisis paso a paso: Desplazamientos y Reducciones");
        desc.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        desc.setForeground(Color.DARK_GRAY);
        descPanel.add(desc);
        
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(topPanel, BorderLayout.NORTH);
        northContainer.add(descPanel, BorderLayout.SOUTH);
        add(northContainer, BorderLayout.NORTH);
        
        // Tabla de corrida
        JTable tablaCorrida = new JTable(modeloCorrida);
        tablaCorrida.setFont(new Font("Consolas", Font.PLAIN, 12));
        tablaCorrida.setRowHeight(24);
        
        // Ajustar anchos de columnas
        tablaCorrida.getColumnModel().getColumn(0).setPreferredWidth(300); // PILA
        tablaCorrida.getColumnModel().getColumn(1).setPreferredWidth(350); // ENTRADA
        tablaCorrida.getColumnModel().getColumn(2).setPreferredWidth(400); // ACCIÓN
        
        tablaCorrida.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        JScrollPane scroll = new JScrollPane(tablaCorrida);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        add(scroll, BorderLayout.CENTER);
        
        // Panel inferior con información
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // Verificar si hay errores
        boolean hayError = false;
        for (int i = 0; i < modeloCorrida.getRowCount(); i++) {
            String accion = (String) modeloCorrida.getValueAt(i, 2);
            if (accion != null && accion.startsWith("Error")) {
                hayError = true;
                break;
            }
        }
        
        String mensaje = hayError ? 
            "Análisis terminado con ERRORES | Total de pasos: " + modeloCorrida.getRowCount() :
            "Análisis ACEPTADO | Total de pasos: " + modeloCorrida.getRowCount();
            
        JLabel info = new JLabel(mensaje);
        info.setFont(new Font("Segoe UI", Font.BOLD, 12));
        info.setForeground(hayError ? Color.RED : new Color(0, 128, 0));
        bottomPanel.add(info);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}